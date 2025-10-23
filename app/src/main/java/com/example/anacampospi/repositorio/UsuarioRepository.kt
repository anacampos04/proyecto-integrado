package com.example.anacampospi.repositorio

import com.example.anacampospi.modelo.Usuario
import com.example.anacampospi.util.CodigoInvitacionUtil
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Capa de acceso a Firestore para la colección /usuarios.
 * Se ocupa de crear/actualizar el documento del usuario autenticado.
 */

class UsuarioRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val col = db.collection("usuarios")

    /**Crea el doc /usuarios/{uid} si no existe.
     * Si ya existe, actualiza nombre/foto si vienen del proveedor (Google).
     * * Genera un código de invitación único al crear el usuario.
     */
    suspend fun ensureUserDoc(
        uid: String,
        correo: String?,
        nombre: String?,
        foto: String?
    ) {
        val doc = col.document(uid)
        val snap = doc.get().await()
        if (!snap.exists()) {
            // Primer login: crear el documento completo con código único
            val codigoInvitacion = generarCodigoUnico()
            val usuario = Usuario(
                idUsuario = uid,
                correo = correo.orEmpty(),
                nombre = nombre.orEmpty(),
                fotoUrl = foto.orEmpty(),
                codigoInvitacion = codigoInvitacion,
                creadoEn = null // Firestore lo establecerá con @ServerTimestamp
            )
            doc.set(usuario).await()
        } else { //si ya existe refrescar los datos que vengan del proveedor si hay
            val updates = mutableMapOf<String, Any>()
            if (!nombre.isNullOrBlank()) updates["nombre"] = nombre
            if (!foto.isNullOrBlank())   updates["fotoUrl"] = foto
            if (updates.isNotEmpty()) doc.update(updates).await()
        }
    }

    /**
     * Genera un código de invitación único verificando que no exista en la BD.
     * Máximo 10 intentos para evitar loop infinito.
     */
    private suspend fun generarCodigoUnico(): String {
        var intentos = 0
        val maxIntentos = 10

        while (intentos < maxIntentos) {
            val codigo = CodigoInvitacionUtil.generar()

            // Verificar si el código ya existe
            val existente = col
                .whereEqualTo("codigoInvitacion", codigo)
                .limit(1)
                .get()
                .await()

            if (existente.isEmpty) {
                return codigo
            }

            intentos++
        }

        // Fallback: usar timestamp si no se pudo generar único
        return "PCT-${System.currentTimeMillis().toString().takeLast(4)}"
    }

    //Obtiene un usuario por su UID
    suspend fun getUsuario(uid: String): Usuario? {
        return try {
            val snap = col.document(uid).get().await()
            snap.toObject(Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Busca un usuario por código de invitación
     * @return Usuario encontrado o null
     */
    suspend fun buscarPorCodigo(codigo: String): Usuario? {
        return try {
            val codigoNormalizado = CodigoInvitacionUtil.normalizar(codigo)

            val snap = col
                .whereEqualTo("codigoInvitacion", codigoNormalizado)
                .limit(1)
                .get()
                .await()

            if (snap.isEmpty) null
            else snap.documents.first().toObject(Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    //Actualiza las plataformas del usuario
    suspend fun actualizarPlataformas(uid: String, plataformas: List<String>) {
        col.document(uid).update("plataformas", plataformas).await()
    }

    //Actualiza la región del usuario
    suspend fun actualizarRegion(uid: String, region: String) {
        col.document(uid).update("region", region).await()
    }

    //Actualiza el perfil del usuario (nombre y foto)
    suspend fun actualizarPerfil(uid: String, nombre: String?, fotoUrl: String?) {
        val updates = mutableMapOf<String, Any>()
        if (!nombre.isNullOrBlank()) updates["nombre"] = nombre
        if (fotoUrl != null) updates["fotoUrl"] = fotoUrl

        if (updates.isNotEmpty()) {
            col.document(uid).update(updates).await()
        }
    }
}
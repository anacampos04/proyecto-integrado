package com.example.anacampospi.repositorio

import com.example.anacampospi.modelo.Usuario
import com.google.firebase.Timestamp
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
     * Si ya existe, actualiza nombre/foto si vienen del proveedor (Google).*/
    suspend fun ensureUserDoc(
        uid: String,
        correo: String?,
        nombre: String?,
        foto: String?
    ) {
        val doc = col.document(uid)
        val snap = doc.get().await()
        if (!snap.exists()) { //primer login crear el documento completo
            val usuario = Usuario(
                idUsuario = uid,
                correo = correo.orEmpty(),
                nombre = nombre.orEmpty(),
                fotoUrl = foto.orEmpty(),
                creadoEn = Timestamp.now() //esto hay que cambiarlo mas adelante por ServerTimeStamp
            )
            doc.set(usuario).await()
        } else { //si ya existe refrescar los datos que vengan del proveedor si hay
            val updates = mutableMapOf<String, Any>()
            if (!nombre.isNullOrBlank()) updates["nombre"] = nombre
            if (!foto.isNullOrBlank())   updates["fotoUrl"] = foto
            if (updates.isNotEmpty()) doc.update(updates).await()
        }
    }
}
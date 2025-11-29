package com.example.anacampospi.repositorio

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Capa de acceso a FirebaseAuth.
 * Solo autentica. NO escribe en Firestore (eso lo hace UsuarioRepository).
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    //Crea un usuario con email/contraseña y deja la sesión iniciada.
    suspend fun registerEmail(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass).await()
        auth.currentUser?.sendEmailVerification()
    }

    //Inicia sesion con email y contraseña
    suspend fun loginEmail(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).await()
    }

    /**
     * Inicia sesión con Google.
     * Recibe el idToken obtenido del flujo de Google Sign-In.
     */
    suspend fun loginGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    //cierra sesion
    fun logout() = auth.signOut()
    //UID del usuario actualmente autenticado (o null).
    fun currentUid(): String? = auth.currentUser?.uid
    //Objeto de usuario de FirebaseAuth (por si se necesita más campos).
    fun currentUser() = auth.currentUser

    /**
     * Verifica si el usuario actual inició sesión con Google.
     * @return true si el usuario usó Google Sign-In, false en caso contrario
     */
    fun isGoogleUser(): Boolean {
        val user = auth.currentUser ?: return false
        return user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
    }

    /**
     * Reautentica al usuario con su contraseña actual.
     * Necesario antes de operaciones sensibles como cambiar contraseña.
     */
    suspend fun reauthenticate(contrasenaActual: String) {
        val user = auth.currentUser ?: throw Exception("No hay sesión activa")
        val email = user.email ?: throw Exception("Usuario sin email")
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, contrasenaActual)
        user.reauthenticate(credential).await()
    }

    /**
     * Cambia la contraseña del usuario actual.
     * Requiere reautenticación previa.
     */
    suspend fun cambiarContrasena(nuevaContrasena: String) {
        val user = auth.currentUser ?: throw Exception("No hay sesión activa")
        user.updatePassword(nuevaContrasena).await()
    }
}

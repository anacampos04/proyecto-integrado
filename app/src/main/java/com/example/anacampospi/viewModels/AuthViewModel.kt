package com.example.anacampospi.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anacampospi.repositorio.AuthRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel que orquesta el login/registro y garantiza que el doc de usuario existe.
 * Expone un estado simple para que la UI reaccione (loading / error / success).
 */
data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val resetEmailSent: Boolean = false
)

class AuthViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val usuarioRepo: UsuarioRepository = UsuarioRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    // Resetear el estado (útil al volver a login después de logout)
    fun resetState() {
        _state.value = AuthUiState()
    }

    //Registro con email/contraseña + creación/actualización de doc en /usuarios.
    fun register(email: String, password: String, nombre: String) = op {
        try {
            authRepo.registerEmail(email, password) //crea y deja la sesión abierta
            ensureUser(nombreOverride = nombre) //crea o actualiza doc en Firestore
        } catch (e: Exception) {
            // Si falla la creación del documento, eliminar el usuario de Auth
            auth.currentUser?.delete()?.await()
            throw e // Re-lanzar la excepción para que se muestre el error
        }
    }

    //Login con email/contraseña
    fun login(email: String, pass: String) = op {
        authRepo.loginEmail(email, pass)
        ensureUser() //creará el doc si no existe (recuperación automática)
    }

    //Login con Google (recibe el idToken)
    fun loginWithGoogle(idToken: String) = op {
        authRepo.loginGoogle(idToken)
        ensureUser()
    }

    //Garantiza que /usuarios/{uid} existe y está actualizado con los datos básicos (correo, nombre).
    private suspend fun ensureUser(nombreOverride: String? = null) {
        val u = auth.currentUser ?: throw IllegalStateException("User null (no autenticada)")
        // log rápido
        android.util.Log.d("Auth", "UID=${u.uid} email=${u.email}")
        usuarioRepo.ensureUserDoc(
            uid = u.uid,
            correo = u.email,
            nombre = nombreOverride ?: u.displayName
        )
    }

    /**
     * Plantilla para ejecutar operaciones de auth con manejo de estado.
     * - Muestra loading
     * - Atrapa cualquier excepción y la expone en 'error'
     * - Marca 'success' para que la UI navegue a Home
     */
    private fun op(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            try {
                block()
                _state.value = AuthUiState(success = true)
            } catch (e: Exception) {
                _state.value = AuthUiState(error = traducirErrorFirebase(e))
            }
        }
    }

    /**
     * Envía un correo para recuperar la contraseña
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            try {
                auth.sendPasswordResetEmail(email).await()
                _state.value = AuthUiState(
                    success = false, // No navegamos a home
                    error = null,
                    loading = false,
                    resetEmailSent = true
                )
            } catch (e: Exception) {
                _state.value = AuthUiState(error = traducirErrorFirebase(e))
            }
        }
    }

    /**
     * Traduce los errores de Firebase a mensajes amigables en español
     */
    private fun traducirErrorFirebase(e: Exception): String {
        // Si es FirebaseAuthException, usar el código de error
        if (e is FirebaseAuthException) {
            return when (e.errorCode) {
                // Errores de login
                "ERROR_INVALID_CREDENTIAL",
                "ERROR_INVALID_EMAIL",
                "ERROR_WRONG_PASSWORD",
                "ERROR_USER_NOT_FOUND" ->
                    "❌ Email o contraseña incorrectos. Por favor, verifica tus datos."

                // Errores de registro
                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    "📧 Este email ya está registrado. Intenta iniciar sesión."

                "ERROR_WEAK_PASSWORD" ->
                    "🔒 La contraseña es muy débil. Usa al menos 6 caracteres con números."

                // Errores de cuenta
                "ERROR_USER_DISABLED" ->
                    "⛔ Esta cuenta ha sido deshabilitada. Contacta con soporte."

                // Errores de red
                "ERROR_NETWORK_REQUEST_FAILED" ->
                    "📡 Error de conexión. Verifica tu internet e inténtalo de nuevo."

                else -> {
                    // Log para debugging
                    android.util.Log.e("AuthViewModel", "Error code: ${e.errorCode}, message: ${e.message}")
                    "⚠️ ${e.message ?: "Algo salió mal. Inténtalo de nuevo."}"
                }
            }
        }

        // Fallback: buscar en el mensaje
        val errorMessage = e.message ?: ""
        return when {
            errorMessage.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                    errorMessage.contains("invalid-credential", ignoreCase = true) ||
                    errorMessage.contains("invalid-email", ignoreCase = true) ->
                "❌ Email o contraseña incorrectos. Por favor, verifica tus datos."

            errorMessage.contains("email-already-in-use", ignoreCase = true) ->
                "📧 Este email ya está registrado. Intenta iniciar sesión."

            errorMessage.contains("network", ignoreCase = true) ->
                "📡 Error de conexión. Verifica tu internet e inténtalo de nuevo."

            else -> {
                android.util.Log.e("AuthViewModel", "Unhandled error: ${e.javaClass.name}, message: $errorMessage")
                "⚠️ $errorMessage"
            }
        }
    }
}
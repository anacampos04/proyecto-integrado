package com.example.anacampospi.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anacampospi.repositorio.AuthRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que orquesta el login/registro y garantiza que el doc de usuario existe.
 * Expone un estado simple para que la UI reaccione (loading / error / success).
 */
data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AuthViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val usuarioRepo: UsuarioRepository = UsuarioRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    //Registro con email/contraseña + creación/actualización de doc en /usuarios.
    fun register(email: String, pass: String) = op {
        authRepo.registerEmail(email, pass) //crea y deja la sesión abierta
        ensureUser() //crea o actualiza doc en Firestore
    }

    //Login con email/contraseña
    fun login(email: String, pass: String) = op {
        authRepo.loginEmail(email, pass)
        ensureUser()
    }

    //Login con Google (recibe el idToken)
    fun loginWithGoogle(idToken: String) = op {
        authRepo.loginGoogle(idToken)
        ensureUser()
    }

    //Garantiza que /usuarios/{uid} existe y está actualizado con los datos básicos (correo, nombre, foto).
    private suspend fun ensureUser() {
        val u = auth.currentUser ?: throw IllegalStateException("User null (no autenticada)")
        // log rápido
        android.util.Log.d("Auth", "UID=${u.uid} email=${u.email}")
        usuarioRepo.ensureUserDoc(
            uid = u.uid,
            correo = u.email,
            nombre = u.displayName,
            foto = u.photoUrl?.toString()
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
                _state.value = AuthUiState(error = e.message ?: "Error desconocido")
            }
        }
    }
}
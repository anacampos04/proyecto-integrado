package com.example.anacampospi.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.anacampospi.auth.GoogleSignInHelper
import com.example.anacampospi.R
import com.example.anacampospi.ui.componentes.PcTOutlinedTextField


//Por mejorar todavía, creada solo para comprobar que funciona Firebase correctamente
@Composable
fun LoginPantalla(
    vm: AuthViewModel,
    onSuccess: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val state by vm.state.collectAsState()

    // Accedemos al Activity y al web client id para Google
    val ctx = LocalContext.current
    val activity = ctx as Activity
    val webClientId = ctx.getString(R.string.default_web_client_id)

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    // Launcher del Intent de Google Sign-In
    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        runCatching {
            val account = GoogleSignInHelper.getAccountFromIntent(res.data)
            vm.loginWithGoogle(account.idToken!!)
        }
    }

    // Si success cambia a true, navegamos
    LaunchedEffect(state.success) { if (state.success) onSuccess() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Iniciar sesión",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        PcTOutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo"
        )

        PcTOutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = "Contraseña",
            isPassword = true
        )

        Button(
            onClick = { vm.login(email.trim(), pass) },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        OutlinedButton(
            onClick = {
                val intent = GoogleSignInHelper.intent(activity, webClientId)
                googleLauncher.launch(intent)
            },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar con Google")
        }

        TextButton(onClick = onGoToRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("¿No tienes cuenta? Regístrate")
        }

        if (state.loading) {
            LinearProgressIndicator(
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

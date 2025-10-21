package com.example.anacampospi.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

//Por mejorar todavía, creada solo para comprobar que funciona Firebase correctamente
@Composable
fun RegistroPantalla(
    vm: AuthViewModel,
    onSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val state by vm.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    LaunchedEffect(state.success) { if (state.success) onSuccess() }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(email, { email = it }, label = { Text("Correo") }, singleLine = true)
        OutlinedTextField(pass, { pass = it }, label = { Text("Contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation())

        Button(
            onClick = { vm.register(email.trim(), pass) },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Registrarme") }

        TextButton(onClick = onGoToLogin) { Text("¿Ya tienes cuenta? Inicia sesión") }

        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

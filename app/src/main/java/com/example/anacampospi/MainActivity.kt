package com.example.anacampospi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.anacampospi.ui.auth.AuthViewModel
import com.example.anacampospi.ui.auth.LoginPantalla
import com.example.anacampospi.ui.auth.RegistroPantalla
import com.example.anacampospi.ui.theme.PopCornTribuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PopCornTribuTheme {
                val nav = rememberNavController()
                val vm: AuthViewModel = viewModel()

                NavHost(navController = nav, startDestination = "login") {
                    composable("login") {
                        LoginPantalla(
                            vm = vm,
                            onSuccess = {
                                // Navega a Home y elimina Login del back stack
                                nav.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoToRegister = { nav.navigate("register") }
                        )
                    }
                    composable("register") {
                        RegistroPantalla(
                            vm = vm,
                            onSuccess = {
                                nav.navigate("home") {
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onGoToLogin = { nav.popBackStack() }
                        )
                    }
                    composable("home") { HomeScreen() }
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Text("¡Has entrado! 🎬")
}

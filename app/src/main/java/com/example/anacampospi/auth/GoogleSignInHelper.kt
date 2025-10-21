package com.example.anacampospi.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/**
 * Utilidad para encapsular la configuración de Google Sign-In
 * y parsear el resultado del Intent.
 */

object GoogleSignInHelper {
    /**
     * Construye el Intent de Google Sign-In.
     * @param webClientId: viene de R.string.default_web_client_id (generado por google-services).
     */
    fun intent(activity: Activity, webClientId: String): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)  // imprescindible para FirebaseAuth con Google
            .requestEmail() //pedir el email
            .build()
        return GoogleSignIn.getClient(activity, gso).signInIntent
    }

    /**
     * Extrae la cuenta Google del Intent de resultado.
     * Lanza ApiException si algo fue mal (y se atrapa en la pantalla).
     */
    fun getAccountFromIntent(data: Intent?): GoogleSignInAccount {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return task.getResult(ApiException::class.java)
    }
}
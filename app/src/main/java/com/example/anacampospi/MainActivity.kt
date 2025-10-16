package com.example.anacampospi

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.anacampospi.ui.theme.AnacampospiTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid
                db.collection("users").document(uid)
                    .set(mapOf("healthcheck" to true))
                    .addOnSuccessListener {
                        Log.i("FS", "OK users/$uid")
                        Toast.makeText(this, "Firestore OK", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("FS", "write error", e)
                        Toast.makeText(this, "ERROR: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FS", "auth error", e)
                Toast.makeText(this, "Auth ERROR: ${e.message}", Toast.LENGTH_LONG).show()
            }
        // UI
        enableEdgeToEdge()
        setContent {
            AnacampospiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AnacampospiTheme {
        Greeting("Android")
    }
}
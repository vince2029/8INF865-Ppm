package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@Composable
fun Login(goToHome: () -> Unit, loginAPI: suspend (String, String) -> Boolean, goToRegister: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Regex simple pour valider l'email
    val isEmailValid = remember(username) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()
    }

    val isPasswordValid = remember(password) {
        password.isNotEmpty()
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.login), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                error = null // On efface l'erreur quand l'utilisateur tape
            },
            label = { Text("Email") },
            singleLine = true,
            isError = username.isNotEmpty() && !isEmailValid,
            supportingText = {
                if (username.isNotEmpty() && !isEmailValid) {
                    Text("Format d'email invalide")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                error = null
            },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = password.isEmpty() && error != null, // Rouge si on a tenté de valider à vide
            supportingText = {
                if (password.isEmpty() && error != null) {
                    Text("Le mot de passe ne peut pas être vide")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!isEmailValid) {
                    error = "Veuillez entrer un email valide"
                    return@Button
                }
                if (!isPasswordValid) {
                    error = "Le mot de passe est requis"
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    error = null

                    try {
                        val success = loginAPI(username, password)
                        if (success) {
                            goToHome()
                        } else {
                            error = "Identifiants invalides"
                        }
                    } catch (e: Exception) {
                        error = "Erreur réseau" + e.message
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Se connecter")
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pas encore de compte ?", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = goToRegister) {
                Text("S'inscrire")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    Login(
        goToHome = {},
        loginAPI = { _, _ -> true },
        goToRegister = {}
    )
}

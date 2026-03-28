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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun Register(
    goToHome: () -> Unit,
    goToLogin: () -> Unit,
    registerAPI: suspend (String, String, String) -> Boolean,
    loginAPI: suspend (String, String) -> Boolean
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val isEmailValid = remember(email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val isPasswordValid = remember(password) { password.length >= 8 }
    val isConfirmValid = remember(password, confirmPassword) { password == confirmPassword }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Créer un compte", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = { Text("Email") },
            singleLine = true,
            isError = email.isNotEmpty() && !isEmailValid,
            supportingText = {
                if (email.isNotEmpty() && !isEmailValid) Text("Format d'email invalide")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; error = null },
            label = { Text("Nom d'utilisateur") },
            singleLine = true,
            isError = username.isEmpty() && error != null,
            supportingText = {
                if (username.isEmpty() && error != null) Text("Le nom d'utilisateur est requis")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = password.isNotEmpty() && !isPasswordValid,
            supportingText = {
                if (password.isNotEmpty() && !isPasswordValid) Text("Minimum 8 caractères")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; error = null },
            label = { Text("Confirmer le mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = confirmPassword.isNotEmpty() && !isConfirmValid,
            supportingText = {
                if (confirmPassword.isNotEmpty() && !isConfirmValid) Text("Les mots de passe ne correspondent pas")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Validation avant appel API
                when {
                    !isEmailValid -> { error = "Veuillez entrer un email valide"; return@Button }
                    username.isEmpty() -> { error = "Le nom d'utilisateur est requis"; return@Button }
                    !isPasswordValid -> { error = "Le mot de passe doit contenir au moins 8 caractères"; return@Button }
                    !isConfirmValid -> { error = "Les mots de passe ne correspondent pas"; return@Button }
                }

                scope.launch {
                    isLoading = true
                    error = null
                    try {
                        val registered = registerAPI(email, username, password)
                        if (registered) {
                            // On connecte directement après l'inscription
                            val loggedIn = loginAPI(email, password)
                            if (loggedIn) goToHome()
                            else goToLogin()
                        } else {
                            error = "Échec de l'inscription (email déjà utilisé ?)"
                        }
                    } catch (e: Exception) {
                        error = "Erreur réseau"
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
                Text("S'inscrire")
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Déjà un compte ?", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = goToLogin) {
                Text("Se connecter")
            }
        }
    }
}


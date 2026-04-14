package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mpp.data.API
import com.example.mpp.data.models.auth.UserProfileModel
import com.example.mpp.data.models.dog.DogModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun Profile( goToLogin: () -> Unit, goToNewDog: () -> Unit) {

    var isLoading by remember { mutableStateOf(true) }
    var userProfile by remember { mutableStateOf<UserProfileModel?>(null) }
    var userDog by remember { mutableStateOf<DogModel?>(null) }
    var editEmail by remember { mutableStateOf("") }
    var editPseudo by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        userProfile = API.getCurrentUserProfile()
        userProfile?.let {
            editEmail = it.email
            editPseudo = it.pseudo
        }
        userDog = API.getDog(API.currentUserId ?: "")
        isLoading = false
    }

    val isEmailValid = remember(editEmail) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(editEmail).matches()
    }
    val isPseudoValid = remember(editPseudo) {
        editPseudo.trim().isNotEmpty()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Mon profil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        if (isLoading) {
            item {
                CircularProgressIndicator()
            }
            return@LazyColumn
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Informations utilisateur", style = MaterialTheme.typography.titleMedium)
                    Text("ID: ${userProfile?.id ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Text("Rôle: ${userProfile?.role ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Text("Points: ${userProfile?.pointsBalance ?: 0}", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = {
                            editEmail = it
                            message = null
                        },
                        label = { Text("Email") },
                        singleLine = true,
                        isError = editEmail.isNotEmpty() && !isEmailValid,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (editEmail.isNotEmpty() && !isEmailValid) {
                                Text("Format d'email invalide")
                            }
                        }
                    )

                    OutlinedTextField(
                        value = editPseudo,
                        onValueChange = {
                            editPseudo = it
                            message = null
                        },
                        label = { Text("Pseudo") },
                        singleLine = true,
                        isError = editPseudo.isNotEmpty() && !isPseudoValid,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (editPseudo.isNotEmpty() && !isPseudoValid) {
                                Text("Le pseudo est requis")
                            }
                        }
                    )

                    Button(
                        onClick = {
                            when {
                                !isEmailValid -> {
                                    message = "Veuillez entrer un email valide"
                                    return@Button
                                }
                                !isPseudoValid -> {
                                    message = "Le pseudo est requis"
                                    return@Button
                                }
                            }

                            scope.launch {
                                isSaving = true
                                val updated = API.updateCurrentUserProfile(
                                    email = editEmail.trim(),
                                    pseudo = editPseudo.trim(),
                                )
                                if (updated != null) {
                                    userProfile = updated
                                    editEmail = updated.email
                                    editPseudo = updated.pseudo
                                    message = "Profil mis à jour"
                                } else {
                                    message = "Échec de la mise à jour (email/pseudo déjà pris ?)"
                                }
                                isSaving = false
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier le profil",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSaving) "Enregistrement..." else "Enregistrer mon profil")
                    }

                    message?.let {
                        Text(
                            text = it,
                            color = if (it.contains("Échec")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        item {
            Text("Mon chien", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(12.dp))

            if (userDog != null) {
                DogCard(userDog!!)
            } else {
                Text(
                    text = "Aucun chien enregistré",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(12.dp))

                Button(onClick = goToNewDog) {
                    Icon(
                        imageVector = Icons.Filled.Pets,
                        contentDescription = "Ajouter un chien",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajouter un chien")
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = {
                        API.logout()
                        goToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Déconnexion",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Se déconnecter")
                }
            }
        }
    }
}

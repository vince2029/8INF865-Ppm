package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mpp.data.API
import com.example.mpp.data.models.auth.UserProfileModel
import com.example.mpp.data.models.dog.DogModel
import kotlinx.coroutines.launch

@Composable
fun Profile(
    goToLogin: () -> Unit,
    goToNewDog: () -> Unit,
    goToEditProfile: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var userProfile by remember { mutableStateOf<UserProfileModel?>(null) }
    var userDog by remember { mutableStateOf<DogModel?>(null) }

    LaunchedEffect(Unit) {
        userProfile = API.getCurrentUserProfile()
        userDog = API.getDog(API.currentUserId ?: "")
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mon profil",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Pseudo: ${userProfile?.pseudo ?: "-"}")
                Text("Email: ${userProfile?.email ?: "-"}")
                Text("Points: ${userProfile?.pointsBalance ?: 0}")
            }

            Button(
                onClick = goToEditProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Modifier le profil",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Modifier mon profil")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Mon chien",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (userDog != null) {
                DogCard(userDog!!)
            } else {
                Text(
                    text = "Aucun chien enregistré",
                    style = MaterialTheme.typography.bodyMedium
                )

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

            Button(
                onClick = {
                    API.logout()
                    goToLogin()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    var userProfile by remember { mutableStateOf<UserProfileModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var showDiscardChangesDialog by remember { mutableStateOf(false) }

    val hasEdits by remember(email, pseudo, userProfile) {
        mutableStateOf(
            userProfile != null && (
                email != userProfile?.email ||
                    pseudo != userProfile?.pseudo
                )
        )
    }

    LaunchedEffect(Unit) {
        userProfile = API.getCurrentUserProfile()
        userProfile?.let {
            email = it.email
            pseudo = it.pseudo
        }
        isLoading = false
    }

    val isEmailValid = remember(email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val isPseudoValid = remember(pseudo) {
        pseudo.trim().isNotEmpty()
    }

    if (showDiscardChangesDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardChangesDialog = false },
            title = { Text("Êtes-vous sûr ?") },
            text = { Text("Les modifications non enregistrées seront perdues.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardChangesDialog = false
                    onClose()
                }) {
                    Text("Quitter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardChangesDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Modifier mon profil") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasEdits) {
                            showDiscardChangesDialog = true
                        } else {
                            onClose()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Informations à modifier",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        message = null
                    },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = email.isNotEmpty() && !isEmailValid,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (email.isNotEmpty() && !isEmailValid) {
                            Text("Format d'email invalide")
                        }
                    }
                )

                OutlinedTextField(
                    value = pseudo,
                    onValueChange = {
                        pseudo = it
                        message = null
                    },
                    label = { Text("Pseudo") },
                    singleLine = true,
                    isError = pseudo.isNotEmpty() && !isPseudoValid,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (pseudo.isNotEmpty() && !isPseudoValid) {
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
                                email = email.trim(),
                                pseudo = pseudo.trim(),
                            )
                            if (updated != null) {
                                userProfile = updated
                                email = updated.email
                                pseudo = updated.pseudo
                                message = "Profil mis à jour"
                            } else {
                                message = "Échec de la mise à jour"
                            }
                            isSaving = false
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSaving) "Enregistrement..." else "Enregistrer")
                }

                message?.let {
                    Text(
                        text = it,
                        color = if (it.startsWith("Échec")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

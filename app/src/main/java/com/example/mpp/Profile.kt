package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpp.data.API
import com.example.mpp.data.models.dog.DogModel

@Composable
fun Profile( goToLogin: () -> Unit, goToNewDog: () -> Unit) {

    var userDog by remember { mutableStateOf<DogModel?>(null) }

    LaunchedEffect(Unit) {
        userDog = API.getDog(API.currentUserId ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.profile))

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(8.dp))

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

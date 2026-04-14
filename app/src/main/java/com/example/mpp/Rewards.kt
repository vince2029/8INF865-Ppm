package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.mpp.data.models.gamification.RewardModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(onBack: () -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    var rewards by remember { mutableStateOf<List<RewardModel>>(emptyList()) }
    var showHelpSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = API.getRewards()
        if (result != null) {
            rewards = result
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Mes récompenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Aide déblocage récompenses",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            rewards.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Aucune récompense disponible pour le moment.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(rewards) { reward ->
                        RewardCard(reward)
                    }
                }
            }
        }

        if (showHelpSheet) {
            ModalBottomSheet(onDismissRequest = { showHelpSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Comment débloquer une récompense ?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Chaque demande de participation acceptée ajoute 20 points au solde. Une récompense est débloquée quand votre solde atteint ou dépasse son coût en points."
                    )
                    Text(
                        "Le coût de chaque récompense est affiché sur sa carte, avec le nombre de points manquants si elle n'est pas encore débloquée."
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RewardCard(reward: RewardModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = reward.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = reward.partnerName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = reward.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Cout: ${reward.pointsCost} points - Reduction: ${reward.discountLabel}",
                style = MaterialTheme.typography.bodySmall,
            )

            if (reward.isUnlocked) {
                Text(
                    text = "Debloquee",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Text(
                    text = "Encore ${reward.pointsMissing} points pour débloquer",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

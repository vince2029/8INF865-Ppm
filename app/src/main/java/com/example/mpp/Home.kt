package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.dog.DogModel
import com.example.mpp.data.models.gamification.GamificationSummaryModel

@Composable
fun Home(
    goToNewActivity: () -> Unit,
    goToJoinActivity: (String) -> Unit,
    goToRewards: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var activities by remember { mutableStateOf<List<ActivityModel>>(emptyList()) }
    var current_dog by remember { mutableStateOf<DogModel?>(null) }
    var gamificationSummary by remember { mutableStateOf<GamificationSummaryModel?>(null) }

    LaunchedEffect(Unit) {
        val Aresult = API.getActivities()
        if (Aresult != null) {
            activities = Aresult
        }
        val userId = API.currentUserId
        if (userId != null) {
            val Dresult = API.getDog(userId)
            if (Dresult != null) {
                current_dog = Dresult
            }
        }

        gamificationSummary = API.getGamificationSummary()
        isLoading = false
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = goToNewActivity) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "stringResource(R.string.add)"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
        ) {
            item {
                ChallengesSection(
                    summary = gamificationSummary,
                    onRewardsClick = goToRewards,
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Attractions,
                        contentDescription = "Activites recommandées",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Activités recommandées",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                activities.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune autre activité disponible.")
                        }
                    }
                }

                else -> {
                    val filteredActivities =
                        if (current_dog != null)
                            activities.filter { dogRespectsActivity(current_dog!!, it) }
                        else
                            activities
                    items(filteredActivities) { activity ->
                        ActivityListItem(activity = activity) {
                            goToJoinActivity(activity.activityId)
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengesSection(
    summary: GamificationSummaryModel?,
    onRewardsClick: () -> Unit,
) {
    var showHelpSheet by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "Defis",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Défis",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = { showHelpSheet = true }) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "Aide calcul des points",
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Solde: ${summary?.pointsBalance ?: 0} points",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val monthlyProgress = summary?.monthlyProgressRatio ?: 0f
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { monthlyProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(72.dp),
                        strokeWidth = 8.dp,
                    )
                    Text(
                        text = "${(monthlyProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = summary?.monthlyMessage ?: "0/5 balades ce mois-ci. Continuez comme ca !",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { (summary?.rewardProgressRatio ?: 0f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = summary?.nextRewardMessage ?: "3 balade(s) avant la prochaine recompense",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = onRewardsClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Mes récompenses")
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
                    text = "Comment sont calculés mes points ?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(summary?.pointsBalanceFormula ?: "Solde actuel = cumul des points gagnés lors des participations acceptées.")
                Text(summary?.monthlyProgressFormula ?: "Progression mensuelle = activités créées ce mois + participations acceptées ce mois.")
                Text(summary?.nextRewardFormula ?: "Prochaine récompense de progression atteinte toutes les 3 participations acceptées.")
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

fun dogRespectsActivity(dog: DogModel, activity: ActivityModel): Boolean {

    if (dog.energyLevel < activity.minEnergyLevel) return false
    if (dog.energyLevel > activity.maxEnergyLevel) return false

    if (!activity.allowShyDogs && dog.isShy) return false

    val sizes = listOf("PETIT", "MOYEN", "GRAND")

    val dogIndex = sizes.indexOf(dog.size.uppercase())
    val minIndex = sizes.indexOf(activity.minDogSize.uppercase())
    val maxIndex = sizes.indexOf(activity.maxDogSize.uppercase())

    if (dogIndex == -1 || minIndex == -1 || maxIndex == -1) return false
    if (dogIndex < minIndex || dogIndex > maxIndex) return false

    return true
}







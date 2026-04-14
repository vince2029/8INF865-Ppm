package com.example.mpp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpp.data.API
import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.dog.DogModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton



@Composable
fun JoinActivityScreen(
    activityId: String,
    onBack: () -> Unit
) {
    ActivityScreen(
        activityId = activityId,
        onBack = onBack
    )
}

data class UserDog(
    val userName: String,
    val dog: DogModel
)

class ActivityViewModelFactory(
    private val activityId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ActivityViewModel(activityId) as T
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ActivityScreen(
    activityId: String,
    onBack: () -> Unit,
    viewModel: ActivityViewModel = viewModel(
        factory = ActivityViewModelFactory(activityId)
    )
) {
    val activity = viewModel.activityState

    val currentUserId = API.currentUserId
    val myRequest = activity?.participantRequests?.find { it.userId == currentUserId }
    val isCreator = activity?.creatorId == currentUserId
    val isPending = myRequest?.status == "PENDING"
    val isAccepted = myRequest?.status == "ACCEPTED"
    val isRejected = myRequest?.status == "REJECTED"
    val showPending = (isPending || viewModel.requestSent) && !viewModel.hasLeft
    val showAccepted = isAccepted && !viewModel.hasLeft
    val showRejected = isRejected && !viewModel.hasLeft
    val creatorUserDog = viewModel.creatorUserDog
    val acceptedUserDogs = viewModel.acceptedUserDogs
    val errorMessage = viewModel.errorMessage
    val invalidDog = viewModel.invalidDogMessage
    val appBarTitle = if (activity == null) {
        "Détail de l'activité"
    } else {
        "Détail - ${activity.title}"
    }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showCancelRequestDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(appBarTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (activity == null) {
            Text(
                "Chargement...",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ActivityHeader(activity)
            Spacer(Modifier.height(16.dp))
            ActivityDetails(activity)
            Spacer(Modifier.height(16.dp))

            DogSection(
                creatorUserDog = creatorUserDog,
                acceptedUserDogs = acceptedUserDogs
            )

            Spacer(Modifier.height(24.dp))
            when {
                isCreator -> {
                    Column {
                        Text(
                            text = "Vous êtes le créateur de l'activité",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Supprimer l'activité") }

                        errorMessage?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                showAccepted -> {
                    Column {
                        Text(
                            text = "Vous participez déjà",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { showLeaveDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Quitter l'activité") }

                        errorMessage?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                showPending -> {
                    Column {
                        Text(
                            text = "Demande en attente",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFFA000)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { showCancelRequestDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Annuler la demande") }

                        errorMessage?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                showRejected -> {
                    Text(
                        text = "Votre demande a été refusée",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                }
                else -> {
                    Column {



                        invalidDog?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Button(
                            onClick = { viewModel.join() },
                            enabled = viewModel.canJoin,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Rejoindre l'activité")
                        }

                        errorMessage?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }


                    }
                }
            }

        }
        if (showLeaveDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveDialog = false },
                title = { Text("Quitter l'activité") },
                text = { Text("Es-tu sûr de vouloir quitter cette activité?") },
                confirmButton = {
                    TextButton(onClick = {
                        showLeaveDialog = false
                        viewModel.leave()
                    }) { Text("Oui") }
                },
                dismissButton = {
                    TextButton(onClick = { showLeaveDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Supprimer l'activité") },
                text = { Text("Cette action est irréversible. Confirmer la suppression?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.deleteActivity {
                            onBack()
                        }
                    }) { Text("Supprimer") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
        if (showCancelRequestDialog) {
            AlertDialog(
                onDismissRequest = { showCancelRequestDialog = false },
                title = { Text("Annuler la demande") },
                text = { Text("Es-tu sûr de vouloir annuler ta demande de participation?") },
                confirmButton = {
                    TextButton(onClick = {
                        showCancelRequestDialog = false
                        viewModel.cancelRequest(myRequest?.requestId ?: "")
                    }) { Text("Oui") }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelRequestDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}






@Composable
fun DogSection(
    creatorUserDog: UserDog?,
    acceptedUserDogs: List<UserDog>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Chiens", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(12.dp))

        creatorUserDog?.let {
            Text("Chien de ${it.userName}", style = MaterialTheme.typography.bodyLarge)
            DogCard(it.dog)
            Spacer(Modifier.height(16.dp))
        }

        if (acceptedUserDogs.isNotEmpty()) {
            Text("Chiens des participants", style = MaterialTheme.typography.bodyLarge)
            acceptedUserDogs.forEach { userDog ->
                Spacer(Modifier.height(8.dp))
                Text(userDog.userName, style = MaterialTheme.typography.bodyMedium)
                DogCard(userDog.dog)
            }
        }
    }
}

@Composable
fun DogCard(dog: DogModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nom: ${dog.name}", style = MaterialTheme.typography.bodyLarge)
            Text("Âge: ${dog.age} ans")
            Text("Taille: ${dog.size}")
            Text("Énergie: ${dog.energyLevel}")
            Text("Timide: ${if (dog.isShy) "Oui" else "Non"}")
        }
    }
}

@Composable
fun ActivityHeader(activity: ActivityModel) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = activity.title,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Par ${activity.creatorPseudo}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = formatDateTime(activity.dateTime),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun ActivityDetails(activity: ActivityModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            InfoRow("Description", activity.description)
            InfoRow("Location", activity.locationName)

            InfoRow("Nombre de participants désirés", activity.maxParticipants.toString())
            InfoRow("participants inscrits", activity.participantCount.toString())

            InfoRow(
                "Chien timide permis",
                if (activity.allowShyDogs) "Oui" else "Non"
            )

            InfoRow(
                "Niveau d'énergie",
                "${activity.minEnergyLevel} → ${activity.maxEnergyLevel}"
            )

            InfoRow(
                "Taille de chien",
                "${activity.minDogSize} → ${activity.maxDogSize}"
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(8.dp))
    }
}

private fun formatDateTime(dateString: String): String {
    val outputFormatter = DateTimeFormatter.ofPattern("d MMMM 'à' HH'h'mm", Locale.FRENCH)

    val parsedDate = runCatching {
        ZonedDateTime.parse(dateString)
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
    }
    .recoverCatching {
        LocalDateTime.parse(dateString)
            .atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
    }
    .recoverCatching {
        LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            .atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
    }
    .getOrNull()

    return parsedDate?.format(outputFormatter) ?: dateString
}

@Preview(showBackground = true)
@Composable
fun JoinActivityPreview() {
    // Note: Mock context for preview
}

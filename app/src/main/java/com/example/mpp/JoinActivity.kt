package com.example.mpp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mpp.data.API
import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.dog.DogModel
import com.example.mpp.data.models.participations.ParticipantModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun JoinActivityScreen(activityId: String,  navController: NavController) {
    var activity by remember { mutableStateOf<ActivityModel?>(null) }

    LaunchedEffect(activityId) {
        val result = API.getActivity(activityId)
        if (result != null) {
            activity = result
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        when {
            activity != null -> {
                ActivityScreen(activity!!, navController)
            }
        }
    }
}

data class UserDog(
    val userName: String,
    val dog: DogModel
)

@Composable
fun ActivityScreen(
    activity: ActivityModel,
    navController: NavController
) {

    var requestSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasLeft by remember { mutableStateOf(false) }
    var participants by remember { mutableStateOf<List<ParticipantModel>>(emptyList()) }
    var creatorUserDog by remember { mutableStateOf<UserDog?>(null) }
    var acceptedUserDogs by remember { mutableStateOf<List<UserDog>>(emptyList()) }

    val currentUserId = API.currentUserId
    val isCreator = activity.creatorId == currentUserId

    val myRequest = activity.participantRequests.find { it.userId == currentUserId }

    val isPending = myRequest?.status == "PENDING"
    val isAccepted = myRequest?.status == "ACCEPTED"
    val isRejected = myRequest?.status == "REJECTED"

    val showPending = (isPending || requestSent) && !hasLeft
    val showAccepted = isAccepted && !hasLeft
    val showRejected = isRejected && !hasLeft


    LaunchedEffect(activity.activityId) {

        API.getParticipants(activity.activityId)?.let {
            participants = it
        }

        API.getDog(activity.creatorId)?.let { dog ->
            creatorUserDog = UserDog(
                userName = activity.creatorPseudo,
                dog = dog
            )
        }
    }

    LaunchedEffect(participants) {
        val dogs = participants.mapNotNull { participant ->
            API.getDog(participant.participantId)?.let { dog ->
                UserDog(
                    userName = participant.participantPseudo,
                    dog = dog
                )
            }
        }
        acceptedUserDogs = dogs
    }

    Column(
        modifier = Modifier
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
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val success = API.deleteActivity(activity.activityId)
                                if (success) {
                                    withContext(Dispatchers.Main) {
                                        navController.popBackStack()
                                    }
                                } else {
                                    errorMessage = "Impossible de supprimer l'activité"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Supprimer l'activité")
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
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
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val success = API.leaveActivity(activity.activityId)

                                if (success) {
                                    hasLeft = true
                                    requestSent = false
                                    errorMessage = null
                                } else {
                                    errorMessage = "Impossible d'annuler votre participation"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Quitter l'activité")
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
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
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val success = API.cancelParticipationRequest(myRequest?.requestId ?: "1")
                                if (success) {
                                    hasLeft = true
                                    requestSent = false
                                    errorMessage = null
                                } else {
                                    errorMessage = "Impossible d'annuler votre demande"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Annuler la demande")
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
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
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val response = API.joinActivity(activity.activityId)

                                if (response.isSuccessful) {
                                    requestSent = true
                                    hasLeft = false
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    val message = try {
                                        JSONObject(errorBody ?: "").getString("detail")
                                    } catch (e: Exception) {
                                        errorBody
                                    }
                                    errorMessage = message ?: "Erreur inconnue"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Rejoindre l'activité")
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
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
            text = activity.dateTime,
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




@Preview(showBackground = true)
@Composable
fun JoinActivityPreview() {
    JoinActivity(
        Id = "456",
    )
}

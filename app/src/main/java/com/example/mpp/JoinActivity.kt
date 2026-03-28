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
import com.example.mpp.data.API
import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.participations.ParticipantModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun JoinActivityScreen(activityId: String) {
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
                ActivityScreen(activity!!)
            }
        }
    }
}

@Composable
fun ActivityScreen(
    activity: ActivityModel
) {
    var requestSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ActivityHeader(activity)
        Spacer(Modifier.height(16.dp))
        ActivityDetails(activity)
        Spacer(Modifier.height(16.dp))

        var participants by remember { mutableStateOf<List< ParticipantModel>>(emptyList()) }

        LaunchedEffect(Unit) {
            val result = API.getParticipants(activity.activityId)
            if (result != null) {
                participants = result
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp))
            {
                Text("Participants inscrits")
                participants.forEach { participant ->
                    ParticipantCard(participant)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (!requestSent) {
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val response = API.joinActivity(activity.activityId)

                        if (response.isSuccessful) {
                            requestSent = true
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val message = try {
                                JSONObject(errorBody ?: "").getString("detail")
                            } catch (e: Exception) {
                                errorBody // fallback to raw text
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

        } else {
            Text(
                text = "Demande envoyée",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

    }
}

@Composable
fun ParticipantCard(participant: ParticipantModel,) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(participant.participantPseudo)
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

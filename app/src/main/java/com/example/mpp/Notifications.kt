package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpp.data.API
import com.example.mpp.data.models.notification.NotificationModel
import kotlinx.coroutines.launch

@Composable
fun Notifications(goToHome: () -> Unit, onSendNotification: () -> Unit) {
    var notifications by remember { mutableStateOf<List<NotificationModel>>(emptyList()) }
    val scope = rememberCoroutineScope()

    fun loadNotifications() {
        scope.launch {
            val result = API.getNotifications()
            if (result != null) {
                notifications = result
            }
        }
    }

    LaunchedEffect(Unit) {
        loadNotifications()
    }

    // Ségrégation des notifications selon les critères demandés
    val receivedRequests = notifications.filter { it.type == "REQUEST" }
    val sentRequests = notifications.filter {
        it.type == "PARTICIPATION_PENDING" ||
            it.type == "PARTICIPATION_ACCEPTED" ||
            it.type == "PARTICIPATION_REJECTED"
    }
    val otherNotifications = notifications.filter {
        it !in receivedRequests && it !in sentRequests
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.notifications),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Section Demandes reçues
        NotificationSectionHeader("Demandes reçues", receivedRequests.size)
        receivedRequests.forEach { notification ->
            ReceivedRequestCard(
                notification = notification,
                onAccept = {
                    scope.launch {
                        val requestId = notification.relatedRequestId ?: return@launch
                        if (API.decideParticipation(requestId, "ACCEPTED")) loadNotifications()
                    }
                },
                onReject = {
                    scope.launch {
                        val requestId = notification.relatedRequestId ?: return@launch
                        if (API.decideParticipation(requestId, "REJECTED")) loadNotifications()
                    }
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Section Demandes envoyées
        NotificationSectionHeader("Demandes envoyées", sentRequests.size)
        sentRequests.forEach { notification ->
            SentRequestCard(
                notification = notification,
                onCancel = {
                    scope.launch {
                        notification.relatedActivityId?.let { activityId ->
                            if (API.leaveActivity(activityId)) loadNotifications()
                        }
                    }
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Section Autre
        NotificationSectionHeader("Autre", otherNotifications.size)
        otherNotifications.forEach { notification ->
            SimpleNotificationCard(notification)
        }
    }
}

@Composable
fun NotificationSectionHeader(title: String, count: Int) {
    Text(
        text = "$title ($count)",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ReceivedRequestCard(
    notification: NotificationModel,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildReceivedRequestText(notification),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row {
                IconButton(onClick = onAccept) {
                    Icon(Icons.Default.Check, contentDescription = "Accepter")
                }
                IconButton(onClick = onReject) {
                    Icon(Icons.Default.Close, contentDescription = "Refuser",)
                }
            }
        }
    }
}

@Composable
fun SentRequestCard(
    notification: NotificationModel,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildSentRequestText(notification),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (notification.type == "PARTICIPATION_PENDING") {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Annuler", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SimpleNotificationCard(notification: NotificationModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = buildOtherNotificationText(notification), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = notification.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

private fun buildReceivedRequestText(notification: NotificationModel) = buildAnnotatedString {
    val sender = notification.senderPseudo ?: "Utilisateur inconnu"
    val activityName = notification.relatedActivityName ?: "votre evenement"

    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append(sender)
    pop()
    append(" veut rejoindre ")
    append(activityName)
    append(".")
}

private fun buildSentRequestText(notification: NotificationModel) = buildAnnotatedString {
    val activityName = notification.relatedActivityName ?: "l'evenement"
    val sender = notification.senderPseudo ?: "L'organisateur"

    when (notification.type) {
        "PARTICIPATION_PENDING" -> {
            append("Votre demande pour \"")
            append(activityName)
            append("\" est ")
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append("en cours")
            pop()
            append(".")
        }
        "PARTICIPATION_ACCEPTED" -> {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(sender)
            pop()
            append(" a ")
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append("accepté")
            pop()
            append(" votre demande pour \"")
            append(activityName)
            append("\".")
        }
        "PARTICIPATION_REJECTED" -> {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(sender)
            pop()
            append(" a ")
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append("refusé")
            pop()
            append(" votre demande pour \"")
            append(activityName)
            append("\".")
        }
        else -> {
            append("Mise a jour de participation.")
        }
    }
}

private fun buildOtherNotificationText(notification: NotificationModel): String {
    val sender = notification.senderPseudo ?: "Un utilisateur"
    val activityName = notification.relatedActivityName
    return if (activityName != null) {
        "$sender a mis a jour l'evenement $activityName"
    } else {
        "$sender vous a envoye une notification"
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsPreview() {
    Notifications(
        goToHome = {},
        onSendNotification = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ReceivedRequestCardPreview() {
    ReceivedRequestCard(
        notification = NotificationModel(
            id = "1",
            userId = "user1",
            type = "REQUEST",
            relatedActivityId = "act1",
            relatedActivityName = "Balade au parc",
            relatedRequestId = "req1",
            isRead = false,
            createdAt = "2023-10-27T10:00:00Z",
            senderPseudo = "Jean-Michel"
        ),
        onAccept = {},
        onReject = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SentRequestCardPendingPreview() {
    SentRequestCard(
        notification = NotificationModel(
            id = "2",
            userId = "user1",
            type = "PARTICIPATION_PENDING",
            relatedActivityId = "act2",
            relatedActivityName = "Balade au parc",
            isRead = false,
            createdAt = "2023-10-27T11:00:00Z"
        ),
        onCancel = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SentRequestCardAcceptedPreview() {
    SentRequestCard(
        notification = NotificationModel(
            id = "3",
            userId = "user1",
            type = "PARTICIPATION_ACCEPTED",
            relatedActivityId = "act3",
            relatedActivityName = "Foret de Soignes",
            isRead = true,
            createdAt = "2023-10-27T12:00:00Z"
        ),
        onCancel = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SimpleNotificationCardPreview() {
    SimpleNotificationCard(
        notification = NotificationModel(
            id = "4",
            userId = "user1",
            type = "OTHER",
            relatedActivityId = null,
            isRead = true,
            createdAt = "2023-10-26T09:00:00Z"
        )
    )
}

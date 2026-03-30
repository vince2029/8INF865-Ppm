package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpp.data.models.notification.NotificationModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun Notifications(
    goToHome: () -> Unit,
    onSendNotification: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading && notifications.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.notifications),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    item { NotificationSectionHeader("Demandes reçues", receivedRequests.size) }
                    items(receivedRequests, key = { it.id }) { notification ->
                        ReceivedRequestCard(
                            notification = notification,
                            onAccept = {
                                notification.relatedRequestId?.let {
                                    viewModel.decideParticipation(notification.id, it, "ACCEPTED")
                                }
                            },
                            onReject = {
                                notification.relatedRequestId?.let {
                                    viewModel.decideParticipation(notification.id, it, "REJECTED")
                                }
                            }
                        )
                    }

                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

                    item { NotificationSectionHeader("Demandes envoyées", sentRequests.size) }
                    items(sentRequests, key = { it.id }) { notification ->
                        DismissibleNotificationItem(
                            onDismiss = { viewModel.dismissNotification(notification.id) }
                        ) {
                            SentRequestCard(
                                notification = notification,
                                onCancel = {
                                    notification.relatedRequestId?.let {
                                        viewModel.cancelParticipationRequest(notification.id, it)
                                    }
                                }
                            )
                        }
                    }

                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

                    item { NotificationSectionHeader("Autre", otherNotifications.size) }
                    items(otherNotifications, key = { it.id }) { notification ->
                        DismissibleNotificationItem(
                            onDismiss = { viewModel.dismissNotification(notification.id) }
                        ) {
                            SimpleNotificationCard(notification)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DismissibleNotificationItem(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        modifier = Modifier.padding(vertical = 4.dp),
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Marquer comme lu",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        content = { content() }
    )
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
                    Icon(Icons.Default.Check, contentDescription = "Accepter", tint = Color.Gray)
                }
                IconButton(onClick = onReject) {
                    Icon(Icons.Default.Close, contentDescription = "Refuser", tint = Color.Gray)
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
            .fillMaxWidth(),
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
            .fillMaxWidth(),
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
    val bold = SpanStyle(fontWeight = FontWeight.Bold)

    withStyle(bold) { append(sender) }
    append(" veut rejoindre ")
    append(activityName)
    append(".")
}

private fun buildSentRequestText(notification: NotificationModel) = buildAnnotatedString {
    val activityName = notification.relatedActivityName ?: "l'evenement"
    val sender = notification.senderPseudo ?: "L'organisateur"
    val bold = SpanStyle(fontWeight = FontWeight.Bold)

    when (notification.type) {
        "PARTICIPATION_PENDING" -> {
            append("Votre demande pour \"$activityName\" est ")
            withStyle(bold) { append("en cours") }
            append(".")
        }
        "PARTICIPATION_ACCEPTED", "PARTICIPATION_REJECTED" -> {
            val action = if (notification.type == "PARTICIPATION_ACCEPTED") "accepté" else "refusé"
            withStyle(bold) { append(sender) }
            append(" a ")
            withStyle(bold) { append(action) }
            append(" votre demande pour \"$activityName\".")
        }
        else -> append("Mise à jour de participation.")
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

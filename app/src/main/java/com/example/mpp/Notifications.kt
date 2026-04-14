package com.example.mpp

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpp.data.models.notification.NotificationModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun Notifications(
    goToHome: () -> Unit,
    goToJoinActivity: (String) -> Unit,
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isLoading && notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp)
            ) {
                item { NotificationSectionHeader("Demandes reçues", receivedRequests.size) }
                items(receivedRequests, key = { it.id }) { notification ->
                    SwipeToReadNotificationItem(
                        onMarkRead = { viewModel.dismissNotification(notification.id) }
                    ) {
                        NotificationActionCard(
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
                            },
                            onOpenActivity = {
                                notification.relatedActivityId?.let { goToJoinActivity(it) }
                            }
                        )
                    }
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

                item { NotificationSectionHeader("Demandes envoyées", sentRequests.size) }
                items(sentRequests, key = { it.id }) { notification ->
                    SwipeToReadNotificationItem(
                        onMarkRead = { viewModel.dismissNotification(notification.id) }
                    ) {
                        NotificationActionCard(
                            notification = notification,
                            onOpenActivity = {
                                notification.relatedActivityId?.let { goToJoinActivity(it) }
                            },
                            extraAction = if (notification.type == "PARTICIPATION_PENDING") {
                                {
                                    notification.relatedRequestId?.let {
                                        viewModel.cancelParticipationRequest(notification.id, it)
                                    }
                                }
                            } else null
                        )
                    }
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

                item { NotificationSectionHeader("Autre", otherNotifications.size) }
                items(otherNotifications, key = { it.id }) { notification ->
                    SwipeToReadNotificationItem(
                        onMarkRead = { viewModel.dismissNotification(notification.id) }
                    ) {
                        NotificationActionCard(
                            notification = notification,
                            onOpenActivity = {
                                notification.relatedActivityId?.let { goToJoinActivity(it) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeToReadNotificationItem(
    onMarkRead: () -> Unit,
    content: @Composable () -> Unit
) {
    val actionWidth = 88.dp
    val density = LocalDensity.current
    val actionWidthPx = with(density) { actionWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(onClick = {
                scope.launch {
                    offsetX.animateTo(0f)
                    onMarkRead()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Marquer comme lue",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(actionWidthPx) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val nextOffset = (offsetX.value + dragAmount).coerceIn(-actionWidthPx, 0f)
                            scope.launch { offsetX.snapTo(nextOffset) }
                        },
                        onDragEnd = {
                            scope.launch {
                                val targetOffset = if (offsetX.value < -actionWidthPx * 0.5f) {
                                    -actionWidthPx
                                } else {
                                    0f
                                }
                                offsetX.animateTo(targetOffset)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
fun NotificationActionCard(
    notification: NotificationModel,
    onOpenActivity: (() -> Unit)? = null,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    extraAction: (() -> Unit)? = null,
) {
    Card(
        onClick = { onOpenActivity?.invoke() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    )
    {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                when (notification.type) {
                    "REQUEST" -> {
                        Text(
                            text = buildReceivedRequestText(notification),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    "PARTICIPATION_PENDING", "PARTICIPATION_ACCEPTED", "PARTICIPATION_REJECTED" -> {
                        Text(
                            text = buildSentRequestText(notification),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        Text(
                            text = buildOtherNotificationText(notification),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = notification.createdAt,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (onAccept != null && onReject != null && notification.type == "REQUEST") {
                    Row {
                        IconButton(onClick = onAccept) {
                            Icon(Icons.Default.Check, contentDescription = "Accepter", tint = Color.Gray)
                        }
                        IconButton(onClick = onReject) {
                            Icon(Icons.Default.Close, contentDescription = "Refuser", tint = Color.Gray)
                        }
                    }
                } else {
                    if (extraAction != null) {
                        TextButton(onClick = extraAction) {
                            Text("Annuler")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceivedRequestCard(
    notification: NotificationModel,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    NotificationActionCard(
        notification = notification,
        onAccept = onAccept,
        onReject = onReject,
    )
}

@Composable
fun SentRequestCard(
    notification: NotificationModel,
    onCancel: () -> Unit
) {
    NotificationActionCard(
        notification = notification,
        extraAction = onCancel
    )
}

@Composable
fun SimpleNotificationCard(notification: NotificationModel) {
    NotificationActionCard(
        notification = notification,
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
        goToJoinActivity = {},
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

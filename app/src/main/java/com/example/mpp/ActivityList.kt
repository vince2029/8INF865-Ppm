package com.example.mpp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mpp.data.API
import com.example.mpp.data.models.activity.ActivityModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityList(
    goToJoinActivity: (String) -> Unit,
    goToNewActivity: () -> Unit,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mes activités", "Chercher")
    
    var myActivities by remember { mutableStateOf<List<ActivityModel>>(emptyList()) }
    var otherActivities by remember { mutableStateOf<List<ActivityModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val allActivities = API.getActivities() ?: emptyList()
        val userId = API.currentUserId

        val myFiltered = allActivities.map { activity ->
            async {
                val isCreator = activity.creatorId == userId
                if (isCreator) {
                    activity
                } else {
                    val participants = API.getParticipants(activity.activityId)
                    if (participants?.any { it.participantId == userId } == true) {
                        activity
                    } else {
                        null
                    }
                }
            }
        }.awaitAll().filterNotNull()

        myActivities = myFiltered
        otherActivities = allActivities.filter { it !in myFiltered }
        isLoading = false
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { goToNewActivity() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Activités",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color.Black,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val activitiesToShow = if (selectedTabIndex == 0) myActivities else otherActivities
                
                if (activitiesToShow.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (selectedTabIndex == 0) "Aucune activité créée ou rejointe." else "Aucune autre activité disponible.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(activitiesToShow) { activity ->
                            ActivityListItem(activity = activity) {
                                goToJoinActivity(activity.activityId)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityListItem(activity: ActivityModel, onClick: () -> Unit) {
    val isCreator = activity.creatorId == API.currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = activity.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isCreator) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(21.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Créateur",
                            tint = Color(0xFFFFFFFF),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
            
            Text(
                text = activity.description,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatDateTime(activity.dateTime),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = activity.locationName,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
fun ActivityListPreview() {
    ActivityList(
        goToJoinActivity = {},
        goToNewActivity = {}
    )
}

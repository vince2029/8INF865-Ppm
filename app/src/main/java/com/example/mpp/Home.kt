package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.example.mpp.data.API
import com.example.mpp.data.models.activity.ActivityModel

@Composable
fun Home(
    goToNewActivity: () -> Unit,
    goToJoinActivity: (String) -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var activities by remember { mutableStateOf<List<ActivityModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        val result = API.getActivities()
        if (result != null) {
            activities = result
        }
        isLoading = false
    }

    Scaffold(
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
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 80.dp) // avoid FAB overlap
        ) {

            item {
                Text(
                    text = "Activités susceptibles de vous plaire",
                    modifier = Modifier.padding(top = 16.dp)
                )
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
                    items(activities) { activity ->
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






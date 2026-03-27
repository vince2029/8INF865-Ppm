package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mpp.data.API
import com.example.mpp.data.models.activity.ActivityModel

@Composable
fun Home(goToLogin: () -> Unit,
         goToNewActivity: () -> Unit,
         goToJoinActivity: (String) -> Unit,
    ){
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

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = stringResource(R.string.home))
            Button(onClick = goToLogin) {
                Text(text = stringResource(R.string.go_login))
            }

            var activities by remember { mutableStateOf<List<ActivityModel>>(emptyList()) }

            LaunchedEffect(Unit) {
                val result = API.getActivities()
                if (result != null) {
                    activities = result
                }
            }

            Text(
                text = stringResource(R.string.activity_list),
                modifier = Modifier.padding(top = 16.dp)
            )
            activities.forEach { activity ->
                ActivityCard(activity, goToJoinActivity)
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivityModel, goToJoinActivity: (String) -> Unit,) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(activity.title)
        Text(activity.description)
        Text("Location: ${activity.locationName}")
        Text("Date: ${activity.dateTime}")
        Button(onClick = {goToJoinActivity(activity.activityId)}) {
            Text(text = stringResource(R.string.go_join_activity, activity.activityId))
        }
    }
}

package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import com.example.mpp.data.models.dog.DogModel

@Composable
fun Home(
    goToNewActivity: () -> Unit,
    goToJoinActivity: (String) -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var activities by remember { mutableStateOf<List<ActivityModel>>(emptyList()) }
    var current_dog by remember { mutableStateOf<DogModel?>(null) }

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
                Text(text = "Activités susceptibles de vous plaire")
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







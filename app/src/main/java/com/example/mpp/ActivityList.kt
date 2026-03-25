package com.example.mpp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ActivityList(goToHome: () -> Unit,
          goToActivityDetails:  (String) -> Unit,
         goToJoinActivity:  (String) -> Unit,
         goToNewActivity: () -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.activity_list))
        Button(onClick = goToHome) {
            Text(text = stringResource(R.string.go_home))
        }
        Button(onClick = {goToActivityDetails("1")}) {
            Text(text = stringResource(R.string.go_activity_details, "1"))
        }
        Button(onClick = {goToActivityDetails("2")}) {
            Text(text = stringResource(R.string.go_activity_details, "2"))
        }
        Button(onClick = {goToJoinActivity("1")}) {
            Text(text = stringResource(R.string.go_join_activity, "1"))
        }
        Button(onClick = {goToJoinActivity("2")}) {
            Text(text = stringResource(R.string.go_join_activity, "2"))
        }
        Button(onClick = goToNewActivity) {
            Text(text = stringResource(R.string.go_new_activity))
        }
    }
}


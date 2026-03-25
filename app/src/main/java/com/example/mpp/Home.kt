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
fun Home(goToLogin: () -> Unit,
         goToProfile: () -> Unit,
         goToChats: () -> Unit,
         goToPartners: () -> Unit,
         goToNewActivity: () -> Unit,
         goToJoinActivity: (String) -> Unit,
         goToActivityList: () -> Unit,
         goToNotifications: () -> Unit,
    ){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.home))
        Button(onClick = goToLogin) {
            Text(text = stringResource(R.string.go_login))
        }
        Button(onClick = goToProfile) {
            Text(text = stringResource(R.string.go_profile))
        }
        Button(onClick = goToChats) {
            Text(text = stringResource(R.string.go_chats))
        }
        Button(onClick = goToPartners) {
            Text(text = stringResource(R.string.go_partners))
        }
        Button(onClick = goToNewActivity) {
            Text(text = stringResource(R.string.go_new_activity))
        }
        Button(onClick = {goToJoinActivity("1")}) {
            Text(text = stringResource(R.string.go_join_activity, "1"))
        }
        Button(onClick = {goToJoinActivity("2")}) {
            Text(text = stringResource(R.string.go_join_activity, "2"))
        }
        Button(onClick = goToActivityList) {
            Text(text = stringResource(R.string.go_activity_list))
        }
        Button(onClick = goToNotifications) {
            Text(text = stringResource(R.string.go_notifications))
        }
    }
}
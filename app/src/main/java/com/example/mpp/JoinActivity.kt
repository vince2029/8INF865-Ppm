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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun JoinActivity(goToHome: () -> Unit,
                 goToActivityList: () -> Unit,
                  activityId: String
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.join_activity, activityId))
        Button(onClick = goToHome) {
            Text(text = stringResource(R.string.go_home))
        }
        Button(onClick = goToActivityList) {
            Text(text = stringResource(R.string.go_activity_list))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JoinActivityPreview() {
    JoinActivity(
        goToHome = {},
        goToActivityList = {},
        activityId = "456"
    )
}

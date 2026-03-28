package com.example.mpp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.navigation.createGraph
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mpp.data.API
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Serializable
object Register
@Serializable
object Login

@Serializable
object Home

@Serializable
object Profile

@Serializable
object Partners

@Serializable
object NewActivity

@Serializable
object Chats

@Serializable
object ActivityList

@Serializable
object Notifications

@Serializable
data class ChatsSpecific(val Id: String)

@Serializable
data class JoinActivity(val Id: String)

@Serializable
data class ActivityDetails(val Id: String)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)

        NotificationHelper.createChannel(this)

        setContent {
            val navController = rememberNavController()

            val navGraph = navController.createGraph(startDestination = Login) {
                composable<Register> {
                    Register(
                        goToHome = {navController.navigate(Home)},
                        goToLogin = {navController.navigate(Login)},
                        registerAPI = {email, username, password -> API.register(email, username, password)},
                        loginAPI = {username, password -> API.login(username, password)}
                    )
                }
                
                composable<Login> {
                    Login(
                        goToHome = { navController.navigate(Home) },
                        loginAPI = { username, password -> API.login(username, password) },
                        goToRegister = { navController.navigate(Register)}
                    )
                }

                composable<Home> {
                    Home(
                        goToLogin = { navController.navigate(Login) },
                        goToNewActivity = { navController.navigate(NewActivity) },
                        goToJoinActivity = { activityId ->
                            navController.navigate(
                                JoinActivity(
                                    activityId
                                )
                            )
                        },

                    )
                }

                composable<Profile> {
                    Profile(
                        goToHome = { navController.navigate(Home) },
                        goToLogin = {navController.navigate(Login)}
                    )
                }

                composable<Chats> {
                    Chats(
                        goToHome = { navController.navigate(Home) },
                        goToChatsSpecific = { chatId -> navController.navigate(ChatsSpecific(chatId))},
                    )
                }

                composable<ChatsSpecific> { backStackEntry ->
                    val chatArgs = backStackEntry.toRoute<ChatsSpecific>()
                    ChatsSpecific(
                        chatId = chatArgs.Id,
                        goToHome = { navController.navigate(Home) },
                        goToChats = { navController.navigate(Chats) },
                    )
                }

                composable<Partners> {
                    Partners(
                        goToHome = { navController.navigate(Home) },
                    )
                }

                composable<NewActivity> {
                    NewActivity(
                        goToHome = { navController.navigate(Home) },
                        goToActivityList = { navController.navigate(ActivityList) },
                    )
                }

                composable<JoinActivity> { backStackEntry ->
                    val activityArgs = backStackEntry.toRoute<JoinActivity>()
                    JoinActivity(
                        activityId = activityArgs.Id,
                        goToHome = { navController.navigate(Home) },
                        goToActivityList = { navController.navigate(ActivityList) },
                    )
                }

                composable<ActivityList> {
                    ActivityList(
                        goToHome = { navController.navigate(Home) },
                        goToActivityDetails = { activityId -> navController.navigate(ActivityDetails(activityId))},
                        goToJoinActivity = { activityId -> navController.navigate(JoinActivity(activityId))},
                        goToNewActivity = { navController.navigate(NewActivity) },
                    )
                }

                composable<ActivityDetails> { backStackEntry ->
                    val activityArgs = backStackEntry.toRoute<ActivityDetails>()
                    ActivityDetails(
                        activityId = activityArgs.Id,
                        goToHome = { navController.navigate(Home) },
                        goToActivityList = { navController.navigate(ActivityList) },
                    )
                }

                composable<Notifications> {
                    Notifications(
                        goToHome = { navController.navigate(Home) },
                        onSendNotification = { NotificationHelper.sendTestNotification(this@MainActivity) }
                    )
                }
            }

            val currentPage = navController.currentBackStackEntryAsState().value?.destination?.route
            val pagesWithoutBars = listOf(Login::class.qualifiedName, Register::class.qualifiedName)
            Scaffold(
                topBar = {
                    if (currentPage !in pagesWithoutBars) {
                        TopHeaderBar(navController)
                    }
                },
                bottomBar = {
                    if (currentPage !in pagesWithoutBars) {
                        BottomNavigationBar(navController)
                    }
                }

            ) { innerPadding ->
                NavHost(
                navController = navController,
                graph = navGraph,
                modifier = Modifier.padding(innerPadding)
                )
            }


        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Notifications::class.qualifiedName,
            onClick = { navController.navigate(Notifications) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
            label = { Text(text = stringResource(R.string.notifs)) }
        )

        NavigationBarItem(
            selected = currentRoute == ActivityList::class.qualifiedName,
            onClick = { navController.navigate(ActivityList) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.activity_list)) },
            label = { Text(text = stringResource(R.string.activity_list)) }
        )

        NavigationBarItem(
            selected = currentRoute == Home::class.qualifiedName,
            onClick = { navController.navigate(Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
            label = { Text(text = stringResource(R.string.home)) }
        )

        NavigationBarItem(
            selected = currentRoute == Chats::class.qualifiedName,
            onClick = { navController.navigate(Chats) },
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = stringResource(R.string.chats)) },
            label = { Text(text = stringResource(R.string.chats)) }
        )

        NavigationBarItem(
            selected = currentRoute == Partners::class.qualifiedName,
            onClick = { navController.navigate(Partners) },
            icon = { Icon(Icons.Default.Group, contentDescription = stringResource(R.string.partners)) },
            label = { Text(text = stringResource(R.string.partners)) }
        )
    }
}

@Composable
fun TopHeaderBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text("MILES PETITES PATTES")
        IconButton(onClick = {navController.navigate(Profile) }) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
        }
    }
}


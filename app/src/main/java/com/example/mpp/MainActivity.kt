package com.example.mpp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.createGraph
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mpp.data.API
import kotlinx.serialization.Serializable

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
                composable<Login> {
                    Login(
                        goToHome = { navController.navigate(Home) },
                        loginAPI = { username, password -> API.login(username, password) },
                    )
                }

                composable<Home> {
                    Home(
                        goToLogin = { navController.navigate(Login) },
                        goToProfile = { navController.navigate(Profile) },
                        goToChats = { navController.navigate(Chats) },
                        goToPartners = { navController.navigate(Partners) },
                        goToNewActivity = { navController.navigate(NewActivity) },
                        goToJoinActivity = { activityId ->
                            navController.navigate(
                                JoinActivity(
                                    activityId
                                )
                            )
                        },
                        goToActivityList = { navController.navigate(ActivityList) },
                        goToNotifications = { navController.navigate(Notifications) },
                        onSendNotification = { NotificationHelper.sendTestNotification(this@MainActivity) }
                    )
                }

                composable<Profile> {
                    Profile(
                        goToHome = { navController.navigate(Home) },
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
                    )
                }
            }
            NavHost(
                navController = navController,
                graph = navGraph
            )
        }
    }
}
package com.example.mpp.data

import com.example.mpp.Taille
import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.activity.CreateActivityModel
import com.example.mpp.data.models.auth.RegisterRequest
import com.example.mpp.data.models.dog.DogModel
import com.example.mpp.data.models.dog.NewDogModel
import com.example.mpp.data.models.gamification.GamificationSummaryModel
import com.example.mpp.data.models.gamification.RewardModel
import com.example.mpp.data.models.notification.NotificationModel
import com.example.mpp.data.models.participations.ParticipantModel
import com.example.mpp.data.models.participations.ParticipationDecisionPayload
import com.example.mpp.data.models.participations.ParticipationRequestResponse
import com.example.mpp.data.remote.RetrofitClient
import com.example.mpp.data.session.SessionManager
import retrofit2.Response

object API {
    var currentUserToken: String? = null
    var currentUserId: String? = null

    fun setSession(token: String?, userId: String?) {
        currentUserToken = token
        currentUserId = userId
        SessionManager.saveSession(token, userId)
    }

    suspend fun login(username: String, password: String): Boolean {
        return try {
            val response = RetrofitClient.service.login(username, password)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                setSession(body.token, body.userId)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getActivities(): List<ActivityModel>? {
        return try {
            val response = RetrofitClient.service.getActivities()

            println("STATUS: ${response.code()}")
            println("BODY: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getActivity(activityId: String): ActivityModel? {
        return try {
            val response = RetrofitClient.service.getActivity(activityId)

            println("STATUS: ${response.code()}")
            println("BODY: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun register(email: String, username: String, password: String): Boolean {
        return try {
            val response = RetrofitClient.service.register(
                RegisterRequest(
                    email = email,
                    username = username,
                    password = password
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                setSession(body.token, body.userId)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createNewDog(name: String, age: Int, size: Taille, energyLevel: Int, isShy: Boolean): Boolean{
        return try {
            val requestBody = NewDogModel(
                name = name,
                age = age,
                size = size,
                energyLevel = energyLevel,
                isShy = isShy,
                ownerId = currentUserId,
            )
            val response = RetrofitClient.service.createNewDog(currentUserId?: "", requestBody)
            response.isSuccessful
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun createActivity(
        title: String,
        description: String,
        locationName: String,
        dateTime: String,
        maxParticipants: Int,
        minEnergyLevel: Int,
        maxEnergyLevel: Int,
        allowShyDogs: Boolean,
        minDogSize: String,
        maxDogSize: String
    ): Boolean {
        return try {
            val requestBody = CreateActivityModel(
                title = title,
                description = description,
                locationName = locationName,
                dateTime = dateTime,
                maxParticipants = maxParticipants,
                minEnergyLevel = minEnergyLevel,
                maxEnergyLevel = maxEnergyLevel,
                allowShyDogs = allowShyDogs,
                minDogSize = minDogSize,
                maxDogSize = maxDogSize
            )

            val response = RetrofitClient.service.createActivity(requestBody)
            println("CREATE ACTIVITY STATUS: ${response.code()}")
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun joinActivity(activityId: String): Response<ParticipationRequestResponse> {
        return RetrofitClient.service.postJoinActivity(activityId)
    }


    suspend fun getParticipants(activityId: String): List<ParticipantModel>? {
        return try {
            val response = RetrofitClient.service.getParticipants(activityId)

            println("STATUS: ${response.code()}")
            println("BODY: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getDog(ownerId: String): DogModel? {
        return try {
            val response = RetrofitClient.service.getDog(ownerId)

            println("STATUS: ${response.code()}")
            println("BODY: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getNotifications(): List<NotificationModel>? {
        return try {
            val response = RetrofitClient.service.getNotifications()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return markNotificationAsReadWithResponse(notificationId).isSuccessful
    }

    suspend fun markNotificationAsReadWithResponse(notificationId: String): Response<Unit> {
        return RetrofitClient.service.markNotificationAsRead(notificationId)
    }

    suspend fun decideParticipation(requestId: String, decision: String): Boolean {
        return decideParticipationWithResponse(requestId, decision).isSuccessful
    }

    suspend fun decideParticipationWithResponse(requestId: String, decision: String): Response<ParticipationRequestResponse> {
        val payload = ParticipationDecisionPayload(decision = decision.uppercase())
        return RetrofitClient.service.decideParticipation(requestId, payload)
    }

    suspend fun leaveActivity(activityId: String): Boolean {
        return leaveActivityWithResponse(activityId).isSuccessful
    }

    suspend fun leaveActivityWithResponse(activityId: String): Response<Unit> {
        return RetrofitClient.service.leaveActivity(activityId)
    }

    suspend fun cancelParticipationRequest(requestId: String): Boolean {
        return cancelParticipationRequestWithResponse(requestId).isSuccessful
    }

    suspend fun cancelParticipationRequestWithResponse(requestId: String): Response<ParticipationRequestResponse> {
        return RetrofitClient.service.cancelParticipationRequest(requestId)
    }

    suspend fun deleteActivity(activityId: String): Boolean {
        return try {
            RetrofitClient.service.deleteActivity(activityId).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getGamificationSummary(): GamificationSummaryModel? {
        return try {
            val response = RetrofitClient.service.getGamificationSummary()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getRewards(): List<RewardModel>? {
        return try {
            val response = RetrofitClient.service.getRewards()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun logout() {
        setSession(null, null)
    }
}
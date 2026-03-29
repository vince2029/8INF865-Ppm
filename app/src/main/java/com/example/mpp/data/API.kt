package com.example.mpp.data


import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.activity.CreateActivityModel
import com.example.mpp.data.models.auth.RegisterRequest
import com.example.mpp.data.models.dog.DogModel
import com.example.mpp.data.models.notification.NotificationModel
import com.example.mpp.data.models.participations.ParticipantModel
import com.example.mpp.data.models.participations.ParticipationDecisionPayload
import com.example.mpp.data.models.participations.ParticipationRequestResponse
import com.example.mpp.data.remote.RetrofitClient
import retrofit2.Response

object API {
    var currentUserToken: String? = null
    var currentUserId: String? = null

    suspend fun login(username: String, password: String): Boolean {
        return try {
            val response = RetrofitClient.service.login(username, password)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                currentUserToken = body.token
                currentUserId = body.userId
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
                currentUserToken = body.token
                currentUserId = body.userId
                true
            } else {
                false
            }
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

    suspend fun decideParticipation(requestId: String, decision: String): Boolean {
        return try {
            val payload = ParticipationDecisionPayload(decision = decision.uppercase())
            RetrofitClient.service.decideParticipation(requestId, payload).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun leaveActivity(activityId: String): Boolean {
        return try {
            RetrofitClient.service.leaveActivity(activityId).isSuccessful
        } catch (e: Exception) {
            false
        }
    }


    fun logout() {
        currentUserToken = null
        currentUserId = null
    }
}
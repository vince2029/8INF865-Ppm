package com.example.mpp.data


import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.activity.CreateActivityModel
import com.example.mpp.data.remote.RetrofitClient

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
}
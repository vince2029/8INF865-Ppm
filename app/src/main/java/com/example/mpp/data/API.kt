package com.example.mpp.data


import com.example.mpp.data.models.activity.ActivityModel
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
}
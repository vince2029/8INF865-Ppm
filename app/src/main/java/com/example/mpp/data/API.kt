package com.example.mpp.data


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
}
package com.example.mpp.data.models.auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token")
    val token: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("token_type")
    val tokenType: String
)
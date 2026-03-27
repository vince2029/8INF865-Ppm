package com.example.mpp.data.models.auth

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    @SerializedName("pseudo")
    val username: String,
    val password: String
)
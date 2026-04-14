package com.example.mpp.data.models.auth

import com.google.gson.annotations.SerializedName

data class UpdateUserProfileRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("pseudo")
    val pseudo: String,
)
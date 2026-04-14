package com.example.mpp.data.models.auth

import com.google.gson.annotations.SerializedName

data class UserProfileModel(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("pseudo")
    val pseudo: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("points_balance")
    val pointsBalance: Int,
)
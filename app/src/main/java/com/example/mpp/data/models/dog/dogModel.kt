package com.example.mpp.data.models.dog

import com.google.gson.annotations.SerializedName

data class DogModel(
    val id: String,
    val name: String,
    val age: Int,
    val size: String,
    @SerializedName("energy_level")
    val energyLevel: Int,
    @SerializedName("is_shy")
    val isShy: Boolean,
    @SerializedName("owner_id")
    val ownerId: String
)

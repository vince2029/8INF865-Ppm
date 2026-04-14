package com.example.mpp.data.models.dog

import com.example.mpp.Taille
import com.google.gson.annotations.SerializedName

data class UpdateDogModel(
    val name: String,
    val age: Int,
    val size: Taille,
    @SerializedName("energy_level")
    val energyLevel: Int,
    @SerializedName("is_shy")
    val isShy: Boolean,
)
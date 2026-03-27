package com.example.mpp.data.models.activity

import com.google.gson.annotations.SerializedName

data class CreateActivityModel(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("location_name") val locationName: String,
    @SerializedName("date_time") val dateTime: String,
    @SerializedName("max_participants") val maxParticipants: Int,
    @SerializedName("min_energy_level") val minEnergyLevel: Int,
    @SerializedName("max_energy_level") val maxEnergyLevel: Int,
    @SerializedName("allow_shy_dogs") val allowShyDogs: Boolean,
    @SerializedName("min_dog_size") val minDogSize: String,
    @SerializedName("max_dog_size") val maxDogSize: String
)
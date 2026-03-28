package com.example.mpp.data.models.participations

import com.google.gson.annotations.SerializedName

data class ParticipantModel(
    @SerializedName("id")
    val participantId: String,

    @SerializedName("pseudo")
    val participantPseudo: String,

)
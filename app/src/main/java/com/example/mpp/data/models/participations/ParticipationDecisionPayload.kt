package com.example.mpp.data.models.participations

import com.google.gson.annotations.SerializedName

data class ParticipationDecisionPayload(
    @SerializedName("decision")
    val decision: String // "ACCEPTED" or "REJECTED"
)

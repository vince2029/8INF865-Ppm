package com.example.mpp.data.models.gamification

import com.google.gson.annotations.SerializedName

data class RewardModel(
    @SerializedName("id")
    val id: String,

    @SerializedName("partner_name")
    val partnerName: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("points_cost")
    val pointsCost: Int,

    @SerializedName("discount_label")
    val discountLabel: String,

    @SerializedName("is_unlocked")
    val isUnlocked: Boolean,

    @SerializedName("points_missing")
    val pointsMissing: Int,
)
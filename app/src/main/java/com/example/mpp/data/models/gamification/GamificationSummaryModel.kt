package com.example.mpp.data.models.gamification

import com.google.gson.annotations.SerializedName

data class GamificationSummaryModel(
    @SerializedName("points_balance")
    val pointsBalance: Int,

    @SerializedName("monthly_activity_count")
    val monthlyActivityCount: Int,

    @SerializedName("monthly_activity_goal")
    val monthlyActivityGoal: Int,

    @SerializedName("monthly_progress_ratio")
    val monthlyProgressRatio: Float,

    @SerializedName("monthly_message")
    val monthlyMessage: String,

    @SerializedName("reward_progress_count")
    val rewardProgressCount: Int,

    @SerializedName("reward_progress_goal")
    val rewardProgressGoal: Int,

    @SerializedName("reward_progress_ratio")
    val rewardProgressRatio: Float,

    @SerializedName("remaining_activities_for_next_reward")
    val remainingActivitiesForNextReward: Int,

    @SerializedName("next_reward_message")
    val nextRewardMessage: String,

    @SerializedName("points_per_accepted_activity")
    val pointsPerAcceptedActivity: Int,

    @SerializedName("points_balance_formula")
    val pointsBalanceFormula: String,

    @SerializedName("monthly_progress_formula")
    val monthlyProgressFormula: String,

    @SerializedName("next_reward_formula")
    val nextRewardFormula: String,
)
package com.example.mpp.data.models.notification

import com.google.gson.annotations.SerializedName

data class NotificationModel(
    @SerializedName("id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("related_activity_id")
    val relatedActivityId: String?,

    @SerializedName("related_activity_name")
    val relatedActivityName: String? = null,

    @SerializedName("related_request_id")
    val relatedRequestId: String? = null,

    @SerializedName("is_read")
    val isRead: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("sender_pseudo")
    val senderPseudo: String? = null,

    @SerializedName("receiver_pseudo")
    val receiverPseudo: String? = null,

    @SerializedName("status")
    val status: String? = null
)

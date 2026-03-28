package com.example.mpp.data.models.notification

import com.google.gson.annotations.SerializedName

data class NotificationModel(
    @SerializedName("id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("related_activity_id")
    val relatedActivityId: String?,

    @SerializedName("is_read")
    val isRead: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    // Adding these as they are mentioned in the requirements (pseudo)
    // and common in such APIs, even if not in the minimal snippet.
    // If they aren't there, we'll handle it.
    @SerializedName("sender_pseudo")
    val senderPseudo: String? = null,

    @SerializedName("status")
    val status: String? = null
)

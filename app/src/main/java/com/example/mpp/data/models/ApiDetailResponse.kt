package com.example.mpp.data.models

import com.google.gson.annotations.SerializedName

data class ApiDetailResponse(
    @SerializedName("detail")
    val detail: String
)

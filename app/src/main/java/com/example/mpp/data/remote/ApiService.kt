package com.example.mpp.data.remote

import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.auth.LoginResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"
    ): Response<LoginResponse>

    @GET("activities")
    suspend fun getActivities(): List<ActivityModel>
}
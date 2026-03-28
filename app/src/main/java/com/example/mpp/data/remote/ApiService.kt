package com.example.mpp.data.remote

import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.activity.CreateActivityModel
import com.example.mpp.data.models.auth.LoginResponse
import com.example.mpp.data.models.auth.RegisterRequest
import com.example.mpp.data.models.participations.ParticipantModel
import com.example.mpp.data.models.participations.ParticipationRequestResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"
    ): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponse>

    @GET("activity/list")
    suspend fun getActivities(): Response<List<ActivityModel>>

    @GET("activity/{activity_id}")
    suspend fun getActivity(
        @Path("activity_id") activityId: String
    ): Response<ActivityModel>

    @POST("activity/")
    suspend fun createActivity(
        @Body request: CreateActivityModel
    ): Response<ActivityModel>

    @POST("participation/join/{activity_id}")
    suspend fun postJoinActivity(
        @Path("activity_id") activityId: String
    ): Response<ParticipationRequestResponse>

    @GET("participation/{activity_id}/participants")
    suspend fun getParticipants(
        @Path("activity_id") activityId: String
    ): Response<List<ParticipantModel>>

}
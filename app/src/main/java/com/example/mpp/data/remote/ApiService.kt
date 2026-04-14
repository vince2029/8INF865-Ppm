package com.example.mpp.data.remote

import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.activity.CreateActivityModel
import com.example.mpp.data.models.auth.LoginResponse
import com.example.mpp.data.models.auth.RegisterRequest
import com.example.mpp.data.models.auth.UpdateUserProfileRequest
import com.example.mpp.data.models.auth.UserProfileModel
import com.example.mpp.data.models.dog.DogModel
import com.example.mpp.data.models.dog.NewDogModel
import com.example.mpp.data.models.dog.UpdateDogModel
import com.example.mpp.data.models.gamification.GamificationSummaryModel
import com.example.mpp.data.models.gamification.RewardModel
import com.example.mpp.data.models.notification.NotificationModel
import com.example.mpp.data.models.participations.ParticipantModel
import com.example.mpp.data.models.participations.ParticipationDecisionPayload
import com.example.mpp.data.models.participations.ParticipationRequestResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
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

    @GET("auth/me")
    suspend fun getCurrentUserProfile(): Response<UserProfileModel>

    @PATCH("auth/me")
    suspend fun updateCurrentUserProfile(
        @Body request: UpdateUserProfileRequest
    ): Response<UserProfileModel>

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

    @GET("dog/{owner_id}")
    suspend fun getDog(
        @Path("owner_id") ownerId: String
    ): Response<DogModel>


    @POST("dog/{owner_id}")
    suspend fun createNewDog(
        @Path("owner_id") ownerId: String,
        @Body request: NewDogModel
    ): Response<DogModel>

    @PATCH("dog/{owner_id}")
    suspend fun updateDog(
        @Path("owner_id") ownerId: String,
        @Body request: UpdateDogModel
    ): Response<DogModel>

    @GET("notification/list")
    suspend fun getNotifications(): Response<List<NotificationModel>>

    @PATCH("notification/{notification_id}/read")
    suspend fun markNotificationAsRead(
        @Path("notification_id") notificationId: String
    ): Response<Unit>

    @POST("participation/decide/{request_id}")
    suspend fun decideParticipation(
        @Path("request_id") requestId: String,
        @Body payload: ParticipationDecisionPayload
    ): Response<ParticipationRequestResponse>

    @DELETE("participation/leave/{activity_id}")
    suspend fun leaveActivity(
        @Path("activity_id") activityId: String
    ): Response<Unit>

    @DELETE("participation/cancel/{request_id}")
    suspend fun cancelParticipationRequest(
        @Path("request_id") requestId: String
    ): Response<ParticipationRequestResponse>

    @DELETE("activity/{activity_id}")
    suspend fun deleteActivity(
        @Path("activity_id") activityId: String
    ): Response<Unit>

    @GET("gamification/summary")
    suspend fun getGamificationSummary(): Response<GamificationSummaryModel>

    @GET("gamification/rewards")
    suspend fun getRewards(): Response<List<RewardModel>>

}
package com.example.mpp.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class PhotonResponse(
    @SerializedName("features") val features: List<PhotonFeature>
)

data class PhotonFeature(
    @SerializedName("properties") val properties: PhotonProperties
)

data class PhotonProperties(
    @SerializedName("name") val name: String,
    @SerializedName("city") val city: String?,
    @SerializedName("postcode") val postcode: String?,
    @SerializedName("country") val country: String?
) {
    fun getDisplayName(): String {
        val parts = mutableListOf<String>()
        parts.add(name)
        if (postcode != null) parts.add(postcode)
        if (city != null && city != name) parts.add(city)
        if (country != null) parts.add(country)
        return parts.joinToString(", ")
    }
}

interface PhotonService {
    @GET("api/")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("lang") lang: String = "fr"
    ): PhotonResponse
}

object LocationClient {
    private const val BASE_URL = "https://photon.komoot.io/"

    val service: PhotonService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PhotonService::class.java)
    }
}

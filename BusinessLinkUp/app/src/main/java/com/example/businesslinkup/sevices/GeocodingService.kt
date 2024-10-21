package com.example.businesslinkup.sevices

import com.google.android.gms.maps.model.LatLng
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("geocode/json")
    suspend fun getCoordinates(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GeocodingResponse
}

data class GeocodingResponse(
    val results: List<Result>
) {
    data class Result(
        val geometry: Geometry
    ) {
        data class Geometry(
            val location: Location
        ) {
            data class Location(
                val lat: Double,
                val lng: Double
            )
        }
    }
}

object GeocodingApi {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: GeocodingService = retrofit.create(GeocodingService::class.java)
}

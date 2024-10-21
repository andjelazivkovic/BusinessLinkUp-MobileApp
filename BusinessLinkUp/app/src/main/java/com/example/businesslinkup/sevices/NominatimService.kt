package com.example.businesslinkup.sevices

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1
    ): List<NominatimResult>

    companion object {
        private const val BASE_URL = "https://nominatim.openstreetmap.org/"

        fun create(): NominatimService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(NominatimService::class.java)
        }
    }
}

data class NominatimResult(
    val lat: String,
    val lon: String,
    val display_name: String
)

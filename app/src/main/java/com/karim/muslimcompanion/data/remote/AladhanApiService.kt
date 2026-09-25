package com.karim.muslimcompanion.data.remote

import com.karim.muslimcompanion.data.remote.dto.AladhanResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Aladhan prayer-times API.
 * Docs: https://aladhan.com/prayer-times-api
 */
interface AladhanApiService {

    @GET("v1/timings")
    suspend fun getTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 5,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis() / 1000
    ): AladhanResponse

    companion object {
        const val BASE_URL = "http://api.aladhan.com/"
    }
}

package com.amaya.mytracker

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data models for Jikan API response
data class JikanResponse(
    val data: List<MangaData>
)

data class MangaData(
    val mal_id: Int,
    val title: String,
    val images: Images,
    val chapters: Int?,
    val genres: List<Genre>?
)

data class Genre(
    val name: String
)

data class Images(
    val jpg: ImageUrl
)

data class ImageUrl(
    val image_url: String
)

interface JikanApiService {
    @GET("manga")
    suspend fun searchManga(@Query("q") query: String): JikanResponse

    companion object {
        private const val BASE_URL = "https://api.jikan.moe/v4/"

        fun create(): JikanApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JikanApiService::class.java)
        }
    }
}
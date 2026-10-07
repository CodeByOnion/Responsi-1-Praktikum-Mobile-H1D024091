package com.pemmob.animaxyz.data.network

import com.pemmob.animaxyz.data.model.AnimeDetailResponse
import com.pemmob.animaxyz.data.model.AnimeListResponse
import com.pemmob.animaxyz.data.model.GenreListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface AnimeApiService {
    @GET("anime")
    suspend fun getAnimeList(
        @Query("q") query: String? = null,
        @Query("genres") genres: Int? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25,
        @Query("order_by") orderBy: String? = null,
        @Query("sort") sort: String? = null,
        @Query("sfw") sfw: Boolean = true
    ): AnimeListResponse

    @GET("anime/{id}")
    suspend fun getAnimeDetail(
        @Path("id") id: Int
    ): AnimeDetailResponse

    @GET("genres/anime")
    suspend fun getGenres(): GenreListResponse
}

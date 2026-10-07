package com.pemmob.animaxyz.data.repository

import com.pemmob.animaxyz.data.model.Anime
import com.pemmob.animaxyz.data.model.Genre
import com.pemmob.animaxyz.data.network.ApiClient
import com.pemmob.animaxyz.data.network.AnimeApiService


class AnimeRepository(
    private val apiService: AnimeApiService = ApiClient.api
) {

    suspend fun searchAnime(query: String?, genreId: Int?): List<Anime> {
        val cleanQuery = query?.trim()?.takeIf { it.isNotBlank() }
        val (orderBy, sort) = if (cleanQuery != null) {
            "score" to "desc"
        } else {
            "popularity" to "asc"
        }

        val response = apiService.getAnimeList(
            query = cleanQuery,
            genres = genreId,
            page = 1,
            limit = 25,
            orderBy = orderBy,
            sort = sort,
            sfw = true
        )

        return response.data ?: throw IllegalStateException("Data anime tidak ditemukan.")
    }

    suspend fun getAnimeDetail(id: Int): Anime {
        val response = apiService.getAnimeDetail(id)
        return response.data ?: throw IllegalStateException("Data anime tidak ditemukan.")
    }

    suspend fun getGenres(): List<Genre> {
        val response = apiService.getGenres()
        return response.data ?: emptyList()
    }
}

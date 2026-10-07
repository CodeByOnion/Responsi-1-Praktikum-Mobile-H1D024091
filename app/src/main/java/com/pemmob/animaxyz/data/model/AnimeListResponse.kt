package com.pemmob.animaxyz.data.model

import com.google.gson.annotations.SerializedName

data class AnimeListResponse(
    val data: List<Anime>? = null,
    val pagination: Pagination? = null
)

data class Pagination(
    @SerializedName("last_visible_page")
    val lastVisiblePage: Int? = null,
    @SerializedName("has_next_page")
    val hasNextPage: Boolean? = null
)

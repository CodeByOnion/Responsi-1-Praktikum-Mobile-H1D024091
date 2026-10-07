package com.pemmob.animaxyz.data.model

import com.google.gson.annotations.SerializedName

data class Anime(
    @SerializedName("mal_id")
    val malId: Int,
    val title: String? = null,
    val type: String? = null,
    val score: Double? = null,
    val episodes: Int? = null,
    val status: String? = null,
    val synopsis: String? = null,
    val genres: List<Genre>? = null,
    val images: Images? = null
) {
    val posterUrl: String?
        get() = images?.jpg?.largeImageUrl ?: images?.jpg?.imageUrl
}

data class Images(
    val jpg: ImageUrls? = null
)

data class ImageUrls(
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("large_image_url")
    val largeImageUrl: String? = null
)

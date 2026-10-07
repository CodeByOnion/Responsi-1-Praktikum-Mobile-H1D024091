package com.pemmob.animaxyz.data.model

import com.google.gson.annotations.SerializedName

data class Genre(
    @SerializedName("mal_id")
    val malId: Int,
    val name: String? = null
)

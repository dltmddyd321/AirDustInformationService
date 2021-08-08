package com.example.plugtest.tmcoordinates


import com.google.gson.annotations.SerializedName

data class TmCoodinatesResponse(
    @SerializedName("documents")
    val documents: List<Document>?,
    @SerializedName("meta")
    val meta: Meta?
)
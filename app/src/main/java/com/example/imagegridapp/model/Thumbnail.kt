package com.example.imagegridapp.model

data class Thumbnail(
    val domain: String,
    val basePath: String,
    val key: String
){
    fun getImageUrl(): String {
        return "${domain}/${basePath}/0/${key}"
    }
}


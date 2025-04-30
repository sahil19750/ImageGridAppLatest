package com.example.imagegridapp.repository

import com.example.imagegridapp.model.ApiResponse

// MediaRepository.kt
class MediaRepository(private val apiService: ApiService) {
    suspend fun getMediaCoverages(): List<ApiResponse> {
        return apiService.getMediaCoverages()
    }
}
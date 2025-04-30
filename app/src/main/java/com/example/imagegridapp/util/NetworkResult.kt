package com.example.imagegridapp.util

// Create new file Result.kt
sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String?) : NetworkResult<Nothing>()
}
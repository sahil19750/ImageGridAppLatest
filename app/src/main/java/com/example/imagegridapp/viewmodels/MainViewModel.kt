package com.example.imagegridapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imagegridapp.model.ApiResponse
import com.example.imagegridapp.model.Thumbnail
import com.example.imagegridapp.repository.ImageRepository
import com.example.imagegridapp.repository.MediaRepository
import com.example.imagegridapp.util.NetworkResult // Make sure this import matches your package structure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// MainViewModel.kt
class MainViewModel(private val repository: MediaRepository) : ViewModel() {
    private val _mediaData = MutableLiveData<NetworkResult<List<ApiResponse>>>()
    val mediaData: LiveData<NetworkResult<List<ApiResponse>>> = _mediaData

    fun loadMedia() {
        viewModelScope.launch {
            _mediaData.postValue(NetworkResult.Loading)
            try {
                val result = repository.getMediaCoverages()
                _mediaData.postValue(NetworkResult.Success(result))
            } catch (e: Exception) {
                _mediaData.postValue(NetworkResult.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
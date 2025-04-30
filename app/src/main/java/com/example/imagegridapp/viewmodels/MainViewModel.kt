package com.example.imagegridapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imagegridapp.model.Thumbnail
import com.example.imagegridapp.repository.ImageRepository
import com.example.imagegridapp.util.NetworkResult // Make sure this import matches your package structure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val repository = ImageRepository()
    val thumbnails = MutableLiveData<NetworkResult<List<Thumbnail>>>()

    fun loadThumbnails() {
        thumbnails.value = NetworkResult.Loading
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    repository.getMediaCoverages()
                }
                thumbnails.value = NetworkResult.Success(data)
            } catch (e: Exception) {
                thumbnails.value = NetworkResult.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
package com.example.imagegridapp.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.example.imagegridapp.R
import com.example.imagegridapp.model.ApiResponse
import com.example.imagegridapp.model.ImageCache
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ApiClient(private val cache: ImageCache) {
    private val executor = Executors.newFixedThreadPool(4)
    private val ongoingRequests = ConcurrentHashMap<String, Future<*>>()

    fun loadImage(url: String, imageView: ImageView) {
        // Cancel previous request for this ImageView
        ongoingRequests[imageView.tag]?.cancel(true)

        imageView.tag = url
        val future = executor.submit {
            try {
                val bitmap = loadBitmap(url)
                imageView.post {
                    if (imageView.tag == url) {
                        bitmap?.let {
                            imageView.setImageBitmap(it)
                        } ?: run {
                            imageView.setImageResource(R.drawable.placeholderimage)
                        }
                    }
                }
            } catch (e: Exception) {
                imageView.post {
                    imageView.setImageResource(R.drawable.placeholderimage)
                }
            }
        }
        ongoingRequests[url] = future
    }

    private fun loadBitmap(url: String): Bitmap? {
        // Check memory cache
        cache.getFromMemory(url)?.let { return it }

        // Check disk cache
        cache.getFromDisk(url)?.let {
            cache.putInMemory(url, it)
            return it
        }

        // Network request
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap?.let {
                cache.putInMemory(url, it)
                cache.putInDisk(url, it)
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun fetchImageList(): List<ApiResponse> {
        // Implement your actual API call here
        // This is a mock implementation
        return emptyList()
    }
}
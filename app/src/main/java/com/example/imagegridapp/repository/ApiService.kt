package com.example.imagegridapp.repository

import android.util.Log
import com.example.imagegridapp.model.ApiResponse
import com.example.imagegridapp.model.Thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

// ApiService.kt
class ApiService {
    suspend fun getMediaCoverages(): List<ApiResponse> {
        return withContext(Dispatchers.IO) {
            val url = URL("https://acharyaprashant.org/api/v2/content/misc/media-coverages?limit=100")
            val connection = url.openConnection() as HttpsURLConnection
            try {
                connection.requestMethod = "GET"
                val inputStream = connection.inputStream
                val json = inputStream.bufferedReader().use { it.readText() }
                Log.d("ApiService Sahil", "Response: $json")
                parseJson(json)
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun parseJson(json: String): List<ApiResponse> {
        val jsonArray = JSONArray(json)
        return List(jsonArray.length()) { i ->
            val item = jsonArray.getJSONObject(i)
            ApiResponse(
                id = item.getString("id"),
                title = item.getString("title"),
                thumbnail = parseThumbnail(item.getJSONObject("thumbnail"))
            )
        }
    }

    private fun parseThumbnail(json: JSONObject): Thumbnail {
        Log.d("ApiService Sahil", "Response: ${json.getString("key")}")

        return Thumbnail(
            domain = json.getString("domain"),
            basePath = json.getString("basePath"),
            key = json.getString("key")
        )
    }
}
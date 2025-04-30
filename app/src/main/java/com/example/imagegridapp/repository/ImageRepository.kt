package com.example.imagegridapp.repository

import com.example.imagegridapp.model.Thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class ImageRepository {
    suspend fun getMediaCoverages(): List<Thumbnail> {
        val url = "https://acharyaprashant.org/api/v2/content/misc/media-coverages?limit=100"
        val connection = withContext(Dispatchers.IO) {
            URL(url).openConnection()
        } as HttpURLConnection

        return try {
            connection.inputStream.bufferedReader().use {
                parseThumbnails(it.readText())
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseThumbnails(json: String): List<Thumbnail> {
        return JSONArray(json).let { array ->
            List(array.length()) { i ->
                val item = array.getJSONObject(i)
                val thumb = item.getJSONObject("thumbnail")
                Thumbnail(
                    thumb.getString("domain"),
                    thumb.getString("basePath"),
                    thumb.getString("key")
                )
            }
        }
    }
}
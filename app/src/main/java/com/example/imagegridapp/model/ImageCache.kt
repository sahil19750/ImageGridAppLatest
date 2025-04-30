package com.example.imagegridapp.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files.exists

class ImageCache(context: Context) {
    // Memory cache (1/8th of available memory)
    private val memoryCache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    ) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    // Disk cache directory
    private val diskCacheDir = File(context.cacheDir, "images").apply {
        if (!exists()) mkdirs()
    }

    fun getFromMemory(key: String): Bitmap? = memoryCache.get(key)

    fun putInMemory(key: String, bitmap: Bitmap) {
        memoryCache.put(key, bitmap)
    }

    fun getFromDisk(key: String): Bitmap? {
        val file = File(diskCacheDir, key.hashCode().toString())
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else null
    }

    fun putInDisk(key: String, bitmap: Bitmap) {
        val file = File(diskCacheDir, key.hashCode().toString())
        try {
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
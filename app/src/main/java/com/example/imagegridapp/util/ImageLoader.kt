package com.example.imagegridapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import android.widget.ImageView
import com.example.imagegridapp.R
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object ImageLoader {
    private const val DISK_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
    private const val MEMORY_CACHE_DIVISOR = 8
    private val IO_EXECUTOR = Executors.newSingleThreadExecutor()

    private lateinit var diskCache: File
    private val memoryCache = object : LruCache<String, Bitmap>(calculateMemoryCacheSize()) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
    }

    private val activeJobs = ConcurrentHashMap<ImageView, Job>()

    fun init(context: Context) {
        diskCache = File(context.cacheDir, "images").apply {
            if (!exists()) mkdirs()
        }
        cleanDiskCache()
    }

    fun loadImage(url: String, imageView: ImageView) {
        activeJobs[imageView]?.cancel()
        imageView.tag = url

        // Check memory cache
        memoryCache.get(url)?.let {
            imageView.setImageBitmap(it)
            return
        }

        val job = CoroutineScope(Dispatchers.Main).launch {
            // Check disk cache
            var bitmap = loadFromDisk(url)
            if (bitmap == null) {
                bitmap = withContext(Dispatchers.IO) {
                    try {
                        loadFromNetwork(url)?.also {
                            saveToDisk(url, it)
                            memoryCache.put(url, it)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            if (bitmap != null) {
                if (imageView.tag == url) {
                    imageView.setImageBitmap(bitmap)
                }
                memoryCache.put(url, bitmap)
            } else {
                imageView.setImageResource(R.drawable.brokenimage)
            }
            activeJobs.remove(imageView)
        }

        activeJobs[imageView] = job
    }

    fun cancelRequest(imageView: ImageView) {
        activeJobs[imageView]?.cancel()
        activeJobs.remove(imageView)
    }

    private fun loadFromDisk(url: String): Bitmap? {
        return File(diskCache, hashUrl(url)).takeIf { it.exists() }?.let {
            BitmapFactory.decodeFile(it.absolutePath)
        }
    }

    private fun loadFromNetwork(url: String): Bitmap? {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.inputStream.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun saveToDisk(url: String, bitmap: Bitmap) {
        IO_EXECUTOR.execute {
            val file = File(diskCache, hashUrl(url))
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 80, it)
            }
        }
    }

    private fun cleanDiskCache() {
        IO_EXECUTOR.execute {
            val files = diskCache.listFiles()?.sortedBy { it.lastModified() }
            var totalSize = files?.sumOf { it.length() } ?: 0

            while (totalSize > DISK_CACHE_SIZE && files?.isNotEmpty() == true) {
                val removed = files.first().delete()
                if (removed) {
                    totalSize -= files.first().length()
                }
            }
        }
    }

    private fun hashUrl(url: String): String {
        return BigInteger(1, MessageDigest.getInstance("MD5").digest(url.toByteArray()))
            .toString(16).padStart(32, '0')
    }

    private fun calculateMemoryCacheSize(): Int {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        return maxMemory / MEMORY_CACHE_DIVISOR
    }
}

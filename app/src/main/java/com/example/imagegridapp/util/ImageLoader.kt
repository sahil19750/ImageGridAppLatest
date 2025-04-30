package com.example.imagegridapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import com.example.imagegridapp.R
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.locks.ReentrantLock
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.withLock

object ImageLoader {
    private const val MEMORY_CACHE_PERCENTAGE = 0.25
    private lateinit var memoryCache: LruCache<String, Bitmap>
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)
    private val diskCacheLock = ReentrantLock()
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
            val cacheSize = (maxMemory * MEMORY_CACHE_PERCENTAGE).toInt()

            memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(key: String, bitmap: Bitmap): Int {
                    return bitmap.byteCount / 1024
                }
            }
            initialized = true
        }
    }

    interface Callback {
        fun shouldSetBitmap(targetUrl: String): Boolean
    }

    fun load(url: String, imageView: ImageView, callback: Callback) {
        if (!initialized) throw IllegalStateException("ImageLoader not initialized")

        // Cancel previous request
        cancelLoad(imageView)

        // Check memory cache
        memoryCache.get(url)?.let {
            if (callback.shouldSetBitmap(url)) {
                imageView.setImageBitmap(it)
            }
            return
        }

        // Async load
        val future = executor.submit {
            try {
                val context = imageView.context.applicationContext
                val bitmap = loadFromDisk(url, context) ?: downloadAndCache(url, context)

                bitmap?.let {
                    memoryCache.put(url, it)
                    imageView.post {
                        if (callback.shouldSetBitmap(url)) {
                            imageView.setImageBitmap(it)
                        }
                    }
                }
            } catch (e: Exception) {
                imageView.post {
                    if (callback.shouldSetBitmap(url)) {
                        imageView.setImageResource(R.drawable.brokenimage)
                    }
                }
            }
        }

        imageView.setTag(R.id.image_request, future)
    }

    fun cancelLoad(imageView: ImageView) {
        (imageView.getTag(R.id.image_request) as? Future<*>)?.cancel(true)
        imageView.setTag(R.id.image_request, null)
    }

    private fun loadFromDisk(url: String, context: Context): Bitmap? {
        return diskCacheLock.withLock {
            val file = File(getCacheDir(context), url.hashCode().toString())
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        }
    }

    private fun downloadAndCache(url: String, context: Context): Bitmap? {
        var connection: HttpsURLConnection? = null
        return try {
            connection = URL(url).openConnection() as HttpsURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 20000

            // First pass: Get image dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            connection.inputStream.use { BitmapFactory.decodeStream(it, null, options) }

            // Calculate sample size based on target dimensions
            val targetWidth = 400 // Adjust based on grid size
            val targetHeight = 400
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)

            // Second pass: Load scaled bitmap
            options.inJustDecodeBounds = false
            connection.inputStream.use {
                val bitmap = BitmapFactory.decodeStream(it, null, options)
                bitmap?.let {
                    Bitmap.createScaledBitmap(it, targetWidth, targetHeight, false)
                }
            }?.also {
                cacheToDisk(url, it, context)
            }

        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfWidth / inSampleSize >= reqWidth &&
                halfHeight / inSampleSize >= reqHeight) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
    private fun cacheToDisk(url: String, bitmap: Bitmap, context: Context) {
        diskCacheLock.withLock {
            try {
                val file = File(getCacheDir(context), url.hashCode().toString())
                FileOutputStream(file).use {
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 80, it)
                }
            } catch (e: Exception) {
                // Handle disk write errors
            }
        }
    }

    private fun getCacheDir(context: Context): File {
        return File(context.cacheDir, "image_cache").apply { mkdirs() }
    }

    fun clearCache(context: Context) {
        memoryCache.evictAll()
        getCacheDir(context).deleteRecursively()
    }
}
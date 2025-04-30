package com.example.imagegridapp.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.imagegridapp.R
import com.example.imagegridapp.SquareImageView
import com.example.imagegridapp.model.ApiResponse
import com.example.imagegridapp.util.ImageLoader

class MediaAdapter : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {
    private var items = emptyList<ApiResponse>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: SquareImageView = view.findViewById(R.id.imageView)
        private var currentUrl: String? = null

        fun bind(url: String) {
            currentUrl = url
            imageView.setImageResource(R.drawable.placeholderimage)
            ImageLoader.load(url, imageView, object : ImageLoader.Callback {
                override fun shouldSetBitmap(targetUrl: String): Boolean {
                    return currentUrl == targetUrl && imageView.isAttachedToWindow
                }
            })
        }

        fun clear() {
            currentUrl = null
            imageView.setImageResource(R.drawable.placeholderimage)
            ImageLoader.cancelLoad(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = items[position].thumbnail.getImageUrl()
        holder.bind(url)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.clear()
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<ApiResponse>) {
        val diffResult = DiffUtil.calculateDiff(ImageDiffCallback(items, newItems))
        items = newItems.toList()
        diffResult.dispatchUpdatesTo(this)
    }

    private class ImageDiffCallback(
        private val oldList: List<ApiResponse>,
        private val newList: List<ApiResponse>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos].id == newList[newPos].id
        override fun areContentsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos] == newList[newPos]
    }
}
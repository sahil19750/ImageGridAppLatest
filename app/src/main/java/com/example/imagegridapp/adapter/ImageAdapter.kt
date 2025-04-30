package com.example.imagegridapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.imagegridapp.R
import com.example.imagegridapp.model.Thumbnail
import com.example.imagegridapp.util.ImageLoader

class ImageAdapter : ListAdapter<Thumbnail, ImageAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        ImageLoader.cancelRequest(holder.imageView)
        super.onViewRecycled(holder)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)

        fun bind(thumbnail: Thumbnail) {
            val url = "${thumbnail.domain}/${thumbnail.basePath}/0/${thumbnail.key}"
            imageView.setImageResource(R.drawable.placeholderimage)
            ImageLoader.loadImage(url, imageView)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Thumbnail>() {
        override fun areItemsTheSame(old: Thumbnail, new: Thumbnail) = old.key == new.key
        override fun areContentsTheSame(old: Thumbnail, new: Thumbnail) = old == new
    }
}
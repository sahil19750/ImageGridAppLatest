package com.example.imagegridapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.imagegridapp.R
import com.example.imagegridapp.SquareImageView
import com.example.imagegridapp.model.ApiResponse
import com.example.imagegridapp.repository.ApiClient

class ImageAdapter(
    private val apiClient: ApiClient,
    private var items: List<ApiResponse> = emptyList()
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: SquareImageView = view.findViewById(R.id.imageView)
    }
    fun updateData(newItems: List<ApiResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = items[position].thumbnail.getImageUrl()
        holder.imageView.setImageResource(R.drawable.placeholderimage)
        apiClient.loadImage(url, holder.imageView)
    }

    override fun getItemCount() = items.size
}
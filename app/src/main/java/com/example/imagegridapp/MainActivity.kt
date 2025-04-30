package com.example.imagegridapp

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.imagegridapp.adapter.ImageAdapter
import com.example.imagegridapp.databinding.ActivityMainBinding
import com.example.imagegridapp.util.ImageLoader
import com.example.imagegridapp.util.NetworkResult
import com.example.imagegridapp.util.SpacingDecoration
import com.example.imagegridapp.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ImageLoader.init(application)

        setupRecyclerView()
        observeViewModel()
        loadData()
    }

    private fun setupRecyclerView() {
        binding.imageRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = ImageAdapter()
            addItemDecoration(SpacingDecoration(resources.getDimensionPixelSize(R.dimen.grid_spacing)))
        }
    }

    // MainActivity.kt
    private fun observeViewModel() {
        viewModel.thumbnails.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    (binding.imageRecyclerView.adapter as ImageAdapter).submitList(result.data)
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showError(result.message)
                }
            }
        }
    }

    private fun loadData() {
        viewModel.loadThumbnails()
    }

    private fun showError(message: String?) {
        Snackbar.make(binding.root, message ?: "Error loading images", Snackbar.LENGTH_LONG)
            .setAction("Retry") { loadData() }
            .show()
    }
}


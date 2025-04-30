package com.example.imagegridapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.imagegridapp.adapter.ImageAdapter
import com.example.imagegridapp.adapter.MediaAdapter
import com.example.imagegridapp.databinding.ActivityMainBinding
import com.example.imagegridapp.model.ApiResponse
import com.example.imagegridapp.model.ImageCache
import com.example.imagegridapp.repository.ApiClient
import com.example.imagegridapp.repository.ApiService
import com.example.imagegridapp.repository.MediaRepository
import com.example.imagegridapp.util.ImageLoader
import com.example.imagegridapp.util.NetworkResult
import com.example.imagegridapp.util.SpacingDecoration
import com.example.imagegridapp.viewmodels.MainViewModel
import com.example.imagegridapp.viewmodels.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize dependencies
        val apiService = ApiService()
        val repository = MediaRepository(apiService)
        val factory = ViewModelFactory(repository)


        // Initialize ViewModel
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        // Initialize ImageLoader with application context
        ImageLoader.init(application)

        setupRecyclerView()
        setupObservers()
        loadData()
    }

    private fun setupRecyclerView() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val columnWidth = screenWidth / 3 // For 3-column grid

        binding.imageRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            //setItemViewCacheSize(20)
            adapter = MediaAdapter()
            setHasFixedSize(true)
            //addItemDecoration(SpacingDecoration(columnWidth / 20)) // Dynamic spacing
            itemAnimator = null
        }
    }

    private fun setupObservers() {
        viewModel.mediaData.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> showLoading()
                is NetworkResult.Success -> showData(result.data)
                is NetworkResult.Error -> showError(result.message!!)
            }
        }
    }

    private fun showData(data: List<ApiResponse>) {
        (binding.imageRecyclerView.adapter as MediaAdapter).submitList(data)
        binding.progressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") { loadData() }
            .show()
        binding.progressBar.visibility = View.GONE
    }

    private fun loadData() {
        viewModel.loadMedia()
    }

    override fun onDestroy() {
        super.onDestroy()
        ImageLoader.clearCache(application)
    }
}


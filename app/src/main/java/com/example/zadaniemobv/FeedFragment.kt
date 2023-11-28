package com.example.zadaniemobv

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zadaniemobv.databinding.FragmentFeedBinding
import com.example.zadaniemobv.viewModel.FeedViewModel
import androidx.navigation.ui.R
import androidx.navigation.ui.setupWithNavController
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.viewModel.LocationViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class FeedFragment : Fragment(com.example.zadaniemobv.R.layout.fragment_feed) {
    private lateinit var viewModel: FeedViewModel
    private lateinit var binding: FragmentFeedBinding

    private lateinit var sharedViewModel: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[FeedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var feedAdapter = FeedAdapter(0.0, 0.0)
        sharedViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

        // Setup RecyclerView with the adapter
        binding.feedRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = feedAdapter
        }

        // Observe location updates from sharedViewModel
        sharedViewModel.location.observe(viewLifecycleOwner) { location ->
            val (lat, lon) = location
            // Update adapter with actual location
            feedAdapter = FeedAdapter(lat, lon)
            binding.feedRecyclerview.adapter = feedAdapter

            // Make sure to reload or update your feed items here
            viewModel.feed_items.value?.let { items ->
                feedAdapter.updateItems(items)
            }
        }

        // Observe feed items from viewModel
        viewModel.feed_items.observe(viewLifecycleOwner) { items ->
            Log.d("FeedFragment", "new values $items")
            feedAdapter.updateItems(items ?: emptyList())
        }

        // Existing code for pull-to-refresh and loading indicator
        binding.pullRefresh.setOnRefreshListener {
            viewModel.updateItems()
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.pullRefresh.isRefreshing = it
        }

        // Apply binding
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewModel
        }

    }
}
package com.example.zadaniemobv

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.zadaniemobv.viewModel.ProfileViewModel



class FeedFragment : Fragment(com.example.zadaniemobv.R.layout.fragment_feed) {
    private lateinit var viewModel: FeedViewModel
    private lateinit var binding: FragmentFeedBinding

    private lateinit var sharedViewModel: LocationViewModel
    private lateinit var profileViewModel: ProfileViewModel

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
        if (!isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection available", Toast.LENGTH_SHORT).show()
        }
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var feedAdapter = FeedAdapter(0.0, 0.0)
        sharedViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[ProfileViewModel::class.java]



        // Setup RecyclerView with the adapter
        binding.feedRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = feedAdapter
        }

        // Apply binding
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewModel
        }.also {bnd ->
            if(profileViewModel.sharingLocation.value != null && !profileViewModel.sharingLocation.value!!){
                Toast.makeText(requireContext(), "Location permission is required to show users", Toast.LENGTH_SHORT).show()
                feedAdapter.updateItems(emptyList())
                return
            }
        }
        sharedViewModel.location.observe(viewLifecycleOwner) { location ->
            val (lat, lon) = location
            // Update adapter with actual location
            feedAdapter = FeedAdapter(lat, lon)
            binding.feedRecyclerview.adapter = feedAdapter

            viewModel.feed_items.value?.let { items ->
                feedAdapter.updateItems(items)
            }
        }

        viewModel.feed_items.observe(viewLifecycleOwner) { items ->
            Log.d("FeedFragment", "new values $items")
            feedAdapter.updateItems(items ?: emptyList())
        }

        binding.pullRefresh.setOnRefreshListener {
            viewModel.updateItems()
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.pullRefresh.isRefreshing = it
        }



    }
}
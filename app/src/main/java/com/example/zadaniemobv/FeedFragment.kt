package com.example.zadaniemobv

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FeedFragment : Fragment(R.layout.fragment_feed) {
    private lateinit var viewModel: FeedViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[FeedViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.feed_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val feedAdapter = FeedAdapter()
        recyclerView.adapter = feedAdapter
        viewModel.feed_items.observe(viewLifecycleOwner) { items ->
            // Tu môžete aktualizovať UI podľa hodnoty stringValue
            feedAdapter.updateItems(items)
        }
        viewModel.updateItems(listOf(
            MyItem(androidx.navigation.ui.R.drawable.abc_ic_ab_back_material,"Prvy",1),
            MyItem(androidx.navigation.ui.R.drawable.abc_ic_ab_back_material,"Druhy",2),
            MyItem(androidx.navigation.ui.R.drawable.abc_ic_ab_back_material,"Treti",3),
            )
        )



    }
}
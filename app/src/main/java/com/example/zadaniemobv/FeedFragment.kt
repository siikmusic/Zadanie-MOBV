package com.example.zadaniemobv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zadaniemobv.databinding.FragmentFeedBinding
import com.example.zadaniemobv.viewModel.FeedViewModel
import androidx.navigation.ui.R


class FeedFragment : Fragment(com.example.zadaniemobv.R.layout.fragment_feed) {
    private lateinit var viewModel: FeedViewModel
    private var binding: FragmentFeedBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[FeedViewModel::class.java]
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentFeedBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
        }.also { bnd->
            bnd.textView2.setOnClickListener {

            }
        }

        viewModel = ViewModelProvider(this)[FeedViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(com.example.zadaniemobv.R.id.feed_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val feedAdapter = FeedAdapter()
        recyclerView.adapter = feedAdapter
        viewModel.feed_items.observe(viewLifecycleOwner) { items ->
            // Tu môžete aktualizovať UI podľa hodnoty stringValue
            feedAdapter.updateItems(items)
        }
        viewModel.updateItems(listOf(
            MyItem(R.drawable.abc_ic_ab_back_material,"Prvy",1),
            MyItem(R.drawable.abc_ic_ab_back_material,"Druhy",2),
            MyItem(R.drawable.abc_ic_ab_back_material,"Treti",3),
        )
        )



    }
    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
package com.example.zadaniemobv.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zadaniemobv.R

class MapFragment : Fragment(R.layout.fragment_map) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val feedButton: Button = view.findViewById(R.id.feed_button)
        feedButton.setOnClickListener {
            findNavController().navigate(R.id.action_mapFragment_to_feedFragment)

        }
    }

}
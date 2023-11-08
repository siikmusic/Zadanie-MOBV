package com.example.zadaniemobv.navbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.example.zadaniemobv.R

class Navbar : ConstraintLayout {
    private var active = -1

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    fun setActive(index: Int) {
        active = index
    }

    fun init() {
        val layout =
            LayoutInflater.from(context)
                .inflate(R.layout.widged_bottom_bar, this, false)
        addView(layout)

        layout.findViewById<ImageView>(R.id.map_image).setOnClickListener {
            if (active != MAP) {
                it.findNavController().navigate(R.id.action_to_map)
            }
        }
        layout.findViewById<ImageView>(R.id.feed_image).setOnClickListener {
            if (active != FEED) {
                it.findNavController().navigate(R.id.action_to_feed)
            }
        }
        layout.findViewById<ImageView>(R.id.profile_image).setOnClickListener {
            if (active != PROFILE) {
                it.findNavController().navigate(R.id.action_to_profile)
            }
        }
    }


    companion object {
        const val MAP = 0
        const val FEED = 1
        const val PROFILE = 2
    }
}
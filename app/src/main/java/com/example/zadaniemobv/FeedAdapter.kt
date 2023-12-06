package com.example.zadaniemobv

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.zadaniemobv.entities.UserEntity
import com.example.zadaniemobv.model.DataUser
import com.example.zadaniemobv.utils.ItemDiffCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class FeedAdapter (val lat:Double, val lon:Double) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {
    private var items: List<UserEntity> = listOf()
    private var photoPrefix = "https://upload.mcomputing.eu/";

    class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_item, parent, false)
        return FeedViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val user =items[position]
        holder.itemView.findViewById<TextView>(R.id.item_text).text = user.name
        if(user.photo != ""){
            val photoUrl = photoPrefix + items[position].photo
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val photoUrlLink = URL(photoUrl)
                    val mIcon = BitmapFactory.decodeStream(withContext(Dispatchers.IO) {
                        withContext(Dispatchers.IO) {
                            photoUrlLink.openConnection()
                        }.getInputStream()
                    })

                    withContext(Dispatchers.Main) {
                        holder.itemView.findViewById<ImageView>(R.id.roundedImageView).setImageBitmap(mIcon)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            val dataUser = DataUser(user.name,user.photo,lat,lon,user.radius)
            bundle.putSerializable("user", dataUser)

            val userDetailFragment = UserDetailFragment()
            userDetailFragment.arguments = bundle

            val navController = holder.itemView.findNavController()
            navController.navigate(R.id.action_feedFragment_to_profileUserFragment, bundle)

        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<UserEntity>) {
        val diffCallback = ItemDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

}

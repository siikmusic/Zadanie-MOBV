package com.example.zadaniemobv

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.zadaniemobv.entities.UserEntity
import com.example.zadaniemobv.utils.ItemDiffCallback
import java.net.URL

class FeedAdapter : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {
    private var items: List<UserEntity> = listOf()
    private var photoPrefix = "https://upload.mcomputing.eu/";

    // ViewHolder poskytuje odkazy na zobrazenia v každej položke
    class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // Táto metóda vytvára nový ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_item, parent, false)
        return FeedViewHolder(view)
    }

    // Táto metóda prepojí dáta s ViewHolderom
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.item_text).text = items[position].name
        if(items[position].photo != ""){
            var photoUrl = photoPrefix+items[position].photo;
            var photoUrlLink = URL(photoUrl);
            var mIcon = BitmapFactory.decodeStream(photoUrlLink.openConnection() .getInputStream());
            holder.itemView.findViewById<ImageView>(R.id.roundedImageView).setImageBitmap(mIcon);

        }
        //holder.itemView.findViewById<ImageView>(R.id.roundedImageView).setImageBitmap()
    }

    // Vracia počet položiek v zozname
    override fun getItemCount() = items.size

    fun updateItems(newItems: List<UserEntity>) {
        val diffCallback = ItemDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

}

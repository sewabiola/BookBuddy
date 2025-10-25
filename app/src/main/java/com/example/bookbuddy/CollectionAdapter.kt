package com.example.bookbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CollectionAdapter(private val collections: MutableList<Collection>) :
    RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    inner class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val collectionName: TextView = itemView.findViewById(R.id.collectionNameText)
        val bookRecyclerView: RecyclerView = itemView.findViewById(R.id.bookListRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_collection, parent, false)
        return CollectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val collection = collections[position]
        holder.collectionName.text = collection.name

        holder.bookRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

        val bookAdapter = BookAdapter(collection.books) { bookToDelete ->
            collection.books.remove(bookToDelete)

            if (collection.books.isEmpty()) {
                collections.remove(collection)
                notifyDataSetChanged()
            } else {
                notifyItemChanged(position)
            }
        }

        holder.bookRecyclerView.adapter = bookAdapter
    }

    override fun getItemCount(): Int = collections.size
}
package com.example.bookbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(
    private val bookList: MutableList<Book>,
    private val onDelete: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.bookTitleText)
        val authorText: TextView = itemView.findViewById(R.id.bookAuthorText)
        val statusSpinner: Spinner = itemView.findViewById(R.id.statusSpinner)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.titleText.text = book.title
        holder.authorText.text = "by ${book.author}"

        val statusArray = holder.itemView.context.resources.getStringArray(R.array.reading_status)
        val index = statusArray.indexOf(book.status).takeIf { it >= 0 } ?: 0
        holder.statusSpinner.setSelection(index)

        holder.statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                book.status = statusArray[pos]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        holder.deleteButton.setOnClickListener {
            onDelete(book)
        }
    }

    override fun getItemCount(): Int = bookList.size
}
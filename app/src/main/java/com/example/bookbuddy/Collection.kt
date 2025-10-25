package com.example.bookbuddy

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CollectionActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var authorInput: EditText
    private lateinit var collectionInput: EditText
    private lateinit var addButton: Button
    private lateinit var recyclerView: RecyclerView

    private val collectionList = mutableListOf<Collection>()
    private lateinit var adapter: CollectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_collection)

        titleInput = findViewById(R.id.bookTitleInput)
        authorInput = findViewById(R.id.bookAuthorInput)
        collectionInput = findViewById(R.id.collectionNameInput)
        addButton = findViewById(R.id.addBookButton)
        recyclerView = findViewById(R.id.collectionRecyclerView)

        adapter = CollectionAdapter(collectionList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val author = authorInput.text.toString().trim()
            val collectionName = collectionInput.text.toString().trim()

            if (title.isNotEmpty() && author.isNotEmpty() && collectionName.isNotEmpty()) {
                val newBook = Book(title, author)

                val existingCollection = collectionList.find { it.name.equals(collectionName, true) }

                if (existingCollection != null) {
                    existingCollection.books.add(newBook)
                } else {
                    val newCollection = Collection(collectionName, mutableListOf(newBook))
                    collectionList.add(newCollection)
                }

                adapter.notifyDataSetChanged()

                titleInput.text.clear()
                authorInput.text.clear()
                collectionInput.text.clear()
            }
        }
    }
}
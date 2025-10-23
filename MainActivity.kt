package com.example.bookbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var profileButton: Button
    private lateinit var collectionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profileButton = findViewById(R.id.profileButton)
        collectionButton = findViewById(R.id.collectionButton)

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        collectionButton.setOnClickListener {
            val intent = Intent(this, CollectionActivity::class.java)
            startActivity(intent)
        }
    }
}
package com.example.bookbuddy

import android.app.Activity
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
            startActivityForResult(intent, 1000)
        }

        collectionButton.setOnClickListener {
            val intent = Intent(this, CollectionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            // Handle profile update if needed
            val name = data?.getStringExtra("name")
            val description = data?.getStringExtra("description")
            // You can use these values to update UI or save to preferences
        }
    }
}
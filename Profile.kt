package com.example.bookbuddy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button

    private val IMAGE_PICK_CODE = 1001
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profileImageView)
        nameEditText = findViewById(R.id.nameEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        saveButton = findViewById(R.id.saveButton)

        // Pick profile image
        profileImageView.setOnClickListener {
            pickImageFromGallery()
        }

        // Save profile info and return to main page
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val description = descriptionEditText.text.toString()

            val resultIntent = Intent().apply {
                putExtra("name", name)
                putExtra("description", description)
                putExtra("imageUri", selectedImageUri?.toString())
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish() // Return to MainActivity
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }
}

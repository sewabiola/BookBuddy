package com.example.bookbuddy

data class Book(
    val id: String,
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val category: String = "",
    val coverImageUrl: String = "",
        val readingStatus: String = "Not Started"
)


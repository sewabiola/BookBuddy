package com.example.bookbuddy

data class Book(
    val title: String,
    val author: String,
    var status: String = "Not Started"
)
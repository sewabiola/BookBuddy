package com.example.bookbuddy

data class Collection(
    val name: String,
    val books: MutableList<Book> = mutableListOf()
)

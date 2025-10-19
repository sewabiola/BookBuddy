package com.example.bookbuddy

data class Book(val title: String, val author: String)
data class BookCollection(val title: String, val books: List<Book>)


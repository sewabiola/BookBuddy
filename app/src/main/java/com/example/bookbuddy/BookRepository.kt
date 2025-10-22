package com.example.bookbuddy

/*
 * Task 2-2-4
 * Created this repository to simulate a real data source for books.
 */

class BookRepository {

    // Simulating a real database of books
    private val allBooks = listOf(
        Book("1984", "George Orwell"),
        Book("To Kill a Mockingbird", "Harper Lee"),
        Book("Pride and Prejudice", "Jane Austen"),
        Book("The Great Gatsby", "F. Scott Fitzgerald"),
        Book("The Catcher in the Rye", "J.D. Salinger"),
        Book("The Hobbit", "J.R.R. Tolkien"),
        Book("Gone Girl", "Gillian Flynn"),
        Book("The Housemaid", "Freida McFadden"),
        Book("Of Mice and Men", "John Steinbeck")
    )

    // Categories simulation
    private val categories = listOf("All", "Classics", "Thrillers", "Modern Fiction", "Favorites")

    fun getAllBooks(): List<Book> = allBooks

    fun getCategories(): List<String> = categories

    fun searchBooks(query: String): List<Book> {
        if (query.isBlank()) return allBooks
        return allBooks.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true)
        }
    }

    fun filterByCategory(category: String): List<Book> {
        return when (category) {
            "Classics" -> allBooks.filter { it.author in listOf("Jane Austen", "George Orwell", "J.D. Salinger") }
            "Thrillers" -> allBooks.filter { it.author == "Gillian Flynn" || it.title.contains("Housemaid") }
            "Modern Fiction" -> allBooks.filter { it.author == "Freida McFadden" || it.author == "F. Scott Fitzgerald" }
            "Favorites" -> allBooks.take(3)
            else -> allBooks
        }
    }
}

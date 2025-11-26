package com.example.bookbuddy

object RecommendationEngine {

    fun recommendFor(user: UserProfile, all: List<BookWithCategory>): List<BookWithCategory> {
        val finishedIds = BookBuddyDatabase.getAllBooks()
            .filter { BookBuddyDatabase.getReadingStatus(it.id) == "Read" || BookBuddyDatabase.getReadingStatus(it.id) == "FINISHED" }
            .map { it.id }
            .toSet()

        val finished = all.filter { it.id in finishedIds }

        val topCategories = finished
            .flatMap { it.categories }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }

        val favoriteAuthors = finished
            .groupingBy { it.author }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }

        return all
            .asSequence()
            .sortedWith(
                compareByDescending<BookWithCategory> { book ->
                    book.categories.any { it in topCategories.take(3) }
                }.thenByDescending { book ->
                    book.author in favoriteAuthors.take(3)
                }.thenByDescending { book ->
                    book.rating
                }
            )
            .filterNot { it.id in finishedIds }
            .take(20)
            .toList()
    }
}

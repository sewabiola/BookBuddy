package com.example.bookbuddy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Tasks 2-2-3-1 (Implement book search functionality) and 2-2-3-2 (Implement filter by category of books)

class BookRepository {

    // Google Books API base URL
    private val baseUrl = "https://www.googleapis.com/books/v1/volumes?q="

    // Fetches book categories dynamically from API results
    private val defaultCategories = listOf(
        "All", "Fiction", "Non-Fiction", "Romance", "Thriller",
        "Science Fiction", "Fantasy", "Classics", "Biography"
    )

    fun getCategories(): List<String> = defaultCategories

    //Task 2-2-3-1: Search books directly from Google Books API

    suspend fun searchBookOnline(query: String): List<BookWithCategory> {
        if (query.isBlank()) return emptyList()

        return withContext(Dispatchers.IO) {
            val formattedQuery = query.trim().replace(" ", "+")
            val apiUrl = "$baseUrl$formattedQuery"
            val urlConnection = URL(apiUrl).openConnection() as HttpURLConnection

            try {
                urlConnection.requestMethod = "GET"
                urlConnection.connectTimeout = 10000
                urlConnection.readTimeout = 10000

                val responseCode = urlConnection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    println("⚠️ API request failed with code $responseCode")
                    return@withContext emptyList()
                }

                val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
                parseBooksFromJson(response)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            } finally {
                urlConnection.disconnect()
            }
        }
    }

    //Task 2-2-3-2: Filter books by category.
    // Since the API already supports category queries, this method makes a new API request limited to a given category

    suspend fun filterBooksByCategory(category: String): List<BookWithCategory> {
        if (category == "All") return searchBookOnline("bestsellers")
        return searchBookOnline("subject:$category")
    }

    // Function to parse Google Books API JSON response.

    private fun parseBooksFromJson(jsonResponse: String): List<BookWithCategory> {
        val result = mutableListOf<BookWithCategory>()

        try {
            val json = JSONObject(jsonResponse)
            val items = json.optJSONArray("items") ?: return result

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val volume = item.optJSONObject("volumeInfo") ?: continue

                val id = item.optString("id", "api_$i")
                val title = volume.optString("title", "Unknown Title")
                val authorsArray = volume.optJSONArray("authors")
                val author = authorsArray?.optString(0) ?: "Unknown Author"
                val description = volume.optString("description", "")
                val imageLinks = volume.optJSONObject("imageLinks")
                val imageUrl = imageLinks?.optString("thumbnail") ?: ""
                val categoryArray = volume.optJSONArray("categories")
                val categories = mutableListOf<String>()

                if (categoryArray != null) {
                    for (j in 0 until categoryArray.length()) {
                        categories.add(categoryArray.getString(j))
                    }
                }

                val pageCount = volume.optInt("pageCount", 0)
                val language = volume.optString("language", "Unknown")
                val publishedDate = volume.optString("publishedDate", "0")
                val publishedYear = publishedDate.take(4).toIntOrNull() ?: 0
                val rating = volume.optDouble("averageRating", 0.0).toFloat()

                result.add(
                    BookWithCategory(
                        id = id,
                        title = title,
                        author = author,
                        categories = categories,
                        coverImageUrl = imageUrl,
                        isbn = volume.optJSONArray("industryIdentifiers")
                            ?.optJSONObject(0)?.optString("identifier", "") ?: "",
                        description = description,
                        publishedYear = publishedYear,
                        rating = rating,
                        pageCount = pageCount,
                        language = language
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
}

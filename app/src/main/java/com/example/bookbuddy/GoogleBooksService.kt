package com.example.bookbuddy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Lightweight Google Books API client that keeps all networking inside the
 * data layer so UI screens can focus on rendering state.
 */
object GoogleBooksService {
    private val client = OkHttpClient()

    suspend fun fetchBooks(
        query: String = "bestseller fiction",
        maxResults: Int = 20
    ): List<BookWithCategory> = withContext(Dispatchers.IO) {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.googleapis.com")
            .addPathSegment("books")
            .addPathSegment("v1")
            .addPathSegment("volumes")
            .addQueryParameter("q", query)
            .addQueryParameter("maxResults", maxResults.toString())
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return@withContext client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@use emptyList()
            }
            val payload = response.body?.string() ?: return@use emptyList()
            parseBooks(payload)
        }
    }

    private fun parseBooks(payload: String): List<BookWithCategory> {
        val root = JSONObject(payload)
        val items = root.optJSONArray("items") ?: return emptyList()
        val results = mutableListOf<BookWithCategory>()

        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue
            val volumeInfo = item.optJSONObject("volumeInfo") ?: continue

            val id = item.optString("id", "remote_$i")
            val title = volumeInfo.optString("title", "Untitled")
            val authorsArray = volumeInfo.optJSONArray("authors")
            val author = if (authorsArray != null && authorsArray.length() > 0) {
                (0 until authorsArray.length()).joinToString(", ") { index ->
                    authorsArray.optString(index)
                }
            } else {
                "Unknown author"
            }

            val categoriesArray = volumeInfo.optJSONArray("categories")
            val categories = if (categoriesArray != null) {
                (0 until categoriesArray.length()).map { index ->
                    categoriesArray.optString(index)
                }
            } else emptyList()

            val imageLinks = volumeInfo.optJSONObject("imageLinks")
            val thumbnail = imageLinks?.optString("thumbnail").orEmpty()
            val description = volumeInfo.optString("description").orEmpty()
            val publishedYear = volumeInfo.optString("publishedDate")
                .take(4)
                .toIntOrNull() ?: 0
            val rating = volumeInfo.optDouble("averageRating", 0.0).toFloat()
            val pageCount = volumeInfo.optInt("pageCount", 0)
            val language = volumeInfo.optString("language", "en")
            val isbnArray = volumeInfo.optJSONArray("industryIdentifiers")
            val isbn = if (isbnArray != null && isbnArray.length() > 0) {
                isbnArray.optJSONObject(0)?.optString("identifier").orEmpty()
            } else ""

            results += BookWithCategory(
                id = id,
                title = title,
                author = author,
                categories = categories,
                coverImageUrl = thumbnail,
                isbn = isbn,
                description = description,
                publishedYear = publishedYear,
                rating = rating,
                pageCount = pageCount,
                language = language
            )
        }

        return results
    }
}



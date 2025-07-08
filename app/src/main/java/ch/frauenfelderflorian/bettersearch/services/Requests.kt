package ch.frauenfelderflorian.bettersearch.services

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import ch.frauenfelderflorian.bettersearch.models.SearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.headersOf
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder

suspend fun fetchSuggestions(query: String, engine: SearchEngine): List<String> {
  if (query.isBlank()) return emptyList()

  val request = Request(
    url = "${engine.suggestionUrl}${URLEncoder.encode(query, "UTF-8")}".toHttpUrl(),
    headers = if (engine.id == searchEngineUuid(4)) {
      headersOf(
        "User-Agent", "Mozilla/5.0 (Android 15; Mobile; rv:139.0) Gecko/139.0 Firefox/139.0",
      )
    } else {
      headersOf()
    }
  )

  return withContext(Dispatchers.IO) {
    try {
      OkHttpClient().newCall(request).execute().use {
        if (!it.isSuccessful) return@withContext emptyList()
        val body = it.body.string()

        val json = JSONArray(body)
        val suggestions = if (engine.id == searchEngineUuid(5)) json else json.getJSONArray(1)
        List(suggestions.length()) { i -> suggestions.getString(i) }
      }
    } catch (e: Exception) {
      return@withContext listOf("Error: ${e.localizedMessage}")
    }
  }
}

fun startSearchIntent(context: Context, query: String, engine: SearchEngine) {
  val url = engine.searchUrl + URLEncoder.encode(query, "UTF-8")
  context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()), null)
}

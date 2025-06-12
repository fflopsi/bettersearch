package ch.frauenfelderflorian.bettersearch

import android.app.SearchManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import ch.frauenfelderflorian.bettersearch.services.room.BetterSearchDatabase
import ch.frauenfelderflorian.bettersearch.ui.BetterSearchApp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val db = Room.databaseBuilder(
      context = applicationContext,
      klass = BetterSearchDatabase::class.java,
      name = "bettersearch-database",
    ).build()
    val historyDao = db.historyDao()

    val query = intent?.getStringExtra(SearchManager.QUERY)

    setContent { BetterSearchApp(query = query.orEmpty(), historyDao = historyDao) }
  }
}

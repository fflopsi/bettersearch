package ch.frauenfelderflorian.bettersearch.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import ch.frauenfelderflorian.bettersearch.models.SearchEngine
import ch.frauenfelderflorian.bettersearch.widget.BetterSearchWidget
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object Prefs {
  object Keys {
    val THEME = intPreferencesKey("theme")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_colors")
    val SEARCH_ENGINE = stringPreferencesKey("search_engine")
    val SHOW_PILLS = booleanPreferencesKey("show_pills")
    val PILLS_ENGINES = stringSetPreferencesKey("pills_engines")
    val SUGGEST_HISTORY = booleanPreferencesKey("suggest_history")
    val SUGGEST_HISTORY_ALL_ENGINES = booleanPreferencesKey("suggest_history_all_engines")
    val INTRO_DONE = booleanPreferencesKey("intro_done")
  }

  object Defaults {
    const val THEME = 0
    const val DYNAMIC_COLOR = true
    val SEARCH_ENGINE = searchEngineUuid(0)
    const val SHOW_PILLS = true
    val PILLS_ENGINES = listOf(searchEngineUuid(0), searchEngineUuid(1), searchEngineUuid(2))
    const val SUGGEST_HISTORY = true
    const val SUGGEST_HISTORY_ALL_ENGINES = true
    const val INTRO_DONE = false
  }
}

suspend fun Context.saveTheme(theme: Int) {
  require(theme in 0..2) { "Value $theme is not allowed for theme" }
  dataStore.edit { it[Prefs.Keys.THEME] = theme }
}

val Context.themeFlow
  get() = dataStore.data.map { it[Prefs.Keys.THEME] ?: Prefs.Defaults.THEME }

suspend fun Context.saveDynamicColor(dynamicColor: Boolean) {
  dataStore.edit { it[Prefs.Keys.DYNAMIC_COLOR] = dynamicColor }
}

val Context.dynamicColorFlow
  get() = dataStore.data.map { it[Prefs.Keys.DYNAMIC_COLOR] ?: Prefs.Defaults.DYNAMIC_COLOR }

suspend fun Context.saveSearchEngine(searchEngine: SearchEngine) {
  dataStore.edit { it[Prefs.Keys.SEARCH_ENGINE] = searchEngine.id.toString() }
  BetterSearchWidget().updateAll(this)
}

val Context.searchEngineFlow
  get() = dataStore.data.map {
    UUID.fromString(it[Prefs.Keys.SEARCH_ENGINE] ?: Prefs.Defaults.SEARCH_ENGINE.toString())
  }

suspend fun Context.saveShowPills(showPills: Boolean) {
  dataStore.edit { it[Prefs.Keys.SHOW_PILLS] = showPills }
}

val Context.showPillsFlow
  get() = dataStore.data.map { it[Prefs.Keys.SHOW_PILLS] ?: Prefs.Defaults.SHOW_PILLS }

suspend fun Context.savePillsEngines(engines: List<SearchEngine>) {
  dataStore.edit { pref ->
    pref[Prefs.Keys.PILLS_ENGINES] = engines.map { it.id.toString() }.toSet()
  }
}

val Context.pillsEnginesFlow
  get() = dataStore.data.map { pref ->
    pref[Prefs.Keys.PILLS_ENGINES]?.map { UUID.fromString(it) } ?: Prefs.Defaults.PILLS_ENGINES
  }

suspend fun Context.saveSuggestHistory(suggestHistory: Boolean) {
  dataStore.edit { it[Prefs.Keys.SUGGEST_HISTORY] = suggestHistory }
}

val Context.suggestHistoryFlow
  get() = dataStore.data.map { it[Prefs.Keys.SUGGEST_HISTORY] ?: Prefs.Defaults.SUGGEST_HISTORY }

suspend fun Context.saveSuggestHistoryAllEngines(suggestHistoryAllEngines: Boolean) {
  dataStore.edit { it[Prefs.Keys.SUGGEST_HISTORY_ALL_ENGINES] = suggestHistoryAllEngines }
}

val Context.suggestHistoryAllEnginesFlow
  get() = dataStore.data.map {
    it[Prefs.Keys.SUGGEST_HISTORY_ALL_ENGINES] ?: Prefs.Defaults.SUGGEST_HISTORY_ALL_ENGINES
  }

suspend fun Context.saveIntroDone(introDone: Boolean) {
  dataStore.edit { it[Prefs.Keys.INTRO_DONE] = introDone }
}

val Context.introDoneFlow
  get() = dataStore.data.map { it[Prefs.Keys.INTRO_DONE] ?: Prefs.Defaults.INTRO_DONE }

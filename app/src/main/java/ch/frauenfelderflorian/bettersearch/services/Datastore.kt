package ch.frauenfelderflorian.bettersearch.services

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.frauenfelderflorian.bettersearch.models.SearchEngine
import ch.frauenfelderflorian.bettersearch.models.getSearchEngine
import ch.frauenfelderflorian.bettersearch.widget.BetterSearchWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

fun <T> Context.saver(scope: CoroutineScope, saver: suspend Context.(T) -> Unit): (T) -> Unit =
  { scope.launch { saver(it) } }

data class Setting<T>(val value: T, val saver: (T) -> Unit) {
  operator fun invoke() = value
  operator fun invoke(newValue: T) = saver(newValue)
}

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
    const val INTRO_DONE = true
  }
}

@Composable
fun Context.themeSetting(scope: CoroutineScope) = Setting(
  value = themeFlow.collectAsStateWithLifecycle(initialValue = Prefs.Defaults.THEME).value,
  saver = saver(scope) { saveTheme(it) },
)

@Composable
fun Context.dynamicColorSetting(scope: CoroutineScope) = Setting(
  value = dynamicColorFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.DYNAMIC_COLOR,
  ).value,
  saver = saver(scope) { saveDynamicColor(it) },
)

@Composable
fun Context.searchEngineSetting(scope: CoroutineScope) = Setting(
  value = searchEngineFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SEARCH_ENGINE,
  ).value.getSearchEngine(),
  saver = saver(scope) { saveSearchEngine(it) },
)

@Composable
fun Context.showPillsSetting(scope: CoroutineScope) = Setting(
  value = showPillsFlow.collectAsStateWithLifecycle(initialValue = Prefs.Defaults.SHOW_PILLS).value,
  saver = saver(scope) { saveShowPills(it) },
)

@Composable
fun Context.pillsEnginesSetting(scope: CoroutineScope) = Setting(
  value = pillsEnginesFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.PILLS_ENGINES,
  ).value.map { it.getSearchEngine() },
  saver = saver(scope) {
    // Necessary because saving the same list modified in order does NOT save the list
    savePillsEngines(emptyList())
    savePillsEngines(it)
  },
)

@Composable
fun Context.suggestHistorySetting(scope: CoroutineScope) = Setting(
  value = suggestHistoryFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SUGGEST_HISTORY,
  ).value,
  saver = saver(scope) { saveSuggestHistory(it) },
)

@Composable
fun Context.suggestHistoryAllEnginesSetting(scope: CoroutineScope) = Setting(
  value = suggestHistoryAllEnginesFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SUGGEST_HISTORY_ALL_ENGINES,
  ).value,
  saver = saver(scope) { saveSuggestHistoryAllEngines(it) },
)

private suspend fun Context.saveTheme(theme: Int) {
  require(theme in 0..2) { "Value $theme is not allowed for theme" }
  dataStore.edit { it[Prefs.Keys.THEME] = theme }
}

private val Context.themeFlow
  get() = dataStore.data.map { it[Prefs.Keys.THEME] ?: Prefs.Defaults.THEME }

private suspend fun Context.saveDynamicColor(dynamicColor: Boolean) {
  dataStore.edit { it[Prefs.Keys.DYNAMIC_COLOR] = dynamicColor }
}

private val Context.dynamicColorFlow
  get() = dataStore.data.map { it[Prefs.Keys.DYNAMIC_COLOR] ?: Prefs.Defaults.DYNAMIC_COLOR }

private suspend fun Context.saveSearchEngine(searchEngine: SearchEngine) {
  dataStore.edit { it[Prefs.Keys.SEARCH_ENGINE] = searchEngine.id.toString() }
  BetterSearchWidget().updateAll(this)
}

val Context.searchEngineFlow
  get() = dataStore.data.map {
    UUID.fromString(it[Prefs.Keys.SEARCH_ENGINE] ?: Prefs.Defaults.SEARCH_ENGINE.toString())
  }

private suspend fun Context.saveShowPills(showPills: Boolean) {
  dataStore.edit { it[Prefs.Keys.SHOW_PILLS] = showPills }
}

private val Context.showPillsFlow
  get() = dataStore.data.map { it[Prefs.Keys.SHOW_PILLS] ?: Prefs.Defaults.SHOW_PILLS }

private suspend fun Context.savePillsEngines(engines: List<SearchEngine>) {
  dataStore.edit { pref ->
    pref[Prefs.Keys.PILLS_ENGINES] = engines.map { it.id.toString() }.toSet()
  }
}

private val Context.pillsEnginesFlow
  get() = dataStore.data.map { pref ->
    pref[Prefs.Keys.PILLS_ENGINES]?.map { UUID.fromString(it) } ?: Prefs.Defaults.PILLS_ENGINES
  }

private suspend fun Context.saveSuggestHistory(suggestHistory: Boolean) {
  dataStore.edit { it[Prefs.Keys.SUGGEST_HISTORY] = suggestHistory }
}

private val Context.suggestHistoryFlow
  get() = dataStore.data.map { it[Prefs.Keys.SUGGEST_HISTORY] ?: Prefs.Defaults.SUGGEST_HISTORY }

private suspend fun Context.saveSuggestHistoryAllEngines(suggestHistoryAllEngines: Boolean) {
  dataStore.edit { it[Prefs.Keys.SUGGEST_HISTORY_ALL_ENGINES] = suggestHistoryAllEngines }
}

private val Context.suggestHistoryAllEnginesFlow
  get() = dataStore.data.map {
    it[Prefs.Keys.SUGGEST_HISTORY_ALL_ENGINES] ?: Prefs.Defaults.SUGGEST_HISTORY_ALL_ENGINES
  }

suspend fun Context.saveIntroDone(introDone: Boolean) {
  dataStore.edit { it[Prefs.Keys.INTRO_DONE] = introDone }
}

val Context.introDoneFlow
  get() = dataStore.data.map { it[Prefs.Keys.INTRO_DONE] ?: Prefs.Defaults.INTRO_DONE }

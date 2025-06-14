package ch.frauenfelderflorian.bettersearch.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.frauenfelderflorian.bettersearch.models.searchEngines
import ch.frauenfelderflorian.bettersearch.services.dynamicColorSetting
import ch.frauenfelderflorian.bettersearch.services.fetchSuggestions
import ch.frauenfelderflorian.bettersearch.services.introDoneFlow
import ch.frauenfelderflorian.bettersearch.services.isFuzzyMatch
import ch.frauenfelderflorian.bettersearch.services.pillsEnginesSetting
import ch.frauenfelderflorian.bettersearch.services.room.HistoryDao
import ch.frauenfelderflorian.bettersearch.services.room.HistoryEntry
import ch.frauenfelderflorian.bettersearch.services.saveIntroDone
import ch.frauenfelderflorian.bettersearch.services.searchEngineSetting
import ch.frauenfelderflorian.bettersearch.services.showPillsSetting
import ch.frauenfelderflorian.bettersearch.services.startSearchIntent
import ch.frauenfelderflorian.bettersearch.services.suggestHistoryAllEnginesSetting
import ch.frauenfelderflorian.bettersearch.services.suggestHistorySetting
import ch.frauenfelderflorian.bettersearch.services.themeSetting
import ch.frauenfelderflorian.bettersearch.ui.screens.IntroScreen
import ch.frauenfelderflorian.bettersearch.ui.screens.SearchScreen
import ch.frauenfelderflorian.bettersearch.ui.screens.SettingsScreen
import ch.frauenfelderflorian.bettersearch.ui.theme.BetterSearchTheme
import ch.frauenfelderflorian.bettersearch.widget.BetterSearchWidgetReceiver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun BetterSearchApp(
  historyDao: HistoryDao,
  modifier: Modifier = Modifier,
  query: String = "",
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val navController = rememberNavController()

  val introDone = runBlocking { context.introDoneFlow.first() }
  val engine = context.searchEngineSetting(scope)
  val showPills = context.showPillsSetting(scope)
  val pillsEngines = context.pillsEnginesSetting(scope)
  val suggestHistory = context.suggestHistorySetting(scope)
  val suggestHistoryAllEngines = context.suggestHistoryAllEnginesSetting(scope)
  val theme = context.themeSetting(scope)
  val dynamicColor = context.dynamicColorSetting(scope)

  val historyAll by historyDao.getAll().collectAsStateWithLifecycle(initialValue = emptyList())
  val historyEngine by historyDao.getAllFromEngine(engine().id).collectAsStateWithLifecycle(
    initialValue = emptyList(),
  )
  val history = if (suggestHistoryAllEngines()) historyAll else historyEngine

  val queryValue by rememberSaveable(
    saver = Saver(
      save = { listOf(it.value.text, it.value.selection.start, it.value.selection.end) },
      restore = {
        mutableStateOf(TextFieldState(it[0] as String, TextRange(it[1] as Int, it[2] as Int)))
      },
    )
  ) {
    mutableStateOf(TextFieldState(query))
  }
  var suggestions by rememberSaveable { mutableStateOf(emptyList<String>()) }
  var historySuggestions by rememberSaveable { mutableStateOf(emptyList<HistoryEntry>()) }

  LaunchedEffect(queryValue.text) {
    suggestions = fetchSuggestions(queryValue.text.toString(), engine())
  }
  LaunchedEffect(queryValue.text, history) {
    historySuggestions = if (queryValue.text.isNotBlank() && suggestHistory()) {
      history.sortedByDescending { it.time }.filter {
        it.query.startsWith(queryValue.text.toString()) || isFuzzyMatch(
          queryValue.text.toString(), it.query
        )
      }
    } else if (suggestHistory()) {
      history.sortedByDescending { it.time }.take(5)
    } else {
      emptyList()
    }
  }

  // Delete the 10 oldest entries from the most-used engine if there are more than 10000 in total
  LaunchedEffect(historyAll) {
    if (historyAll.size > 10000) {
      searchEngines.map { historyDao.getAllFromEngine(it.id).first() }.maxByOrNull { it.size }
        ?.take(10)?.forEach { historyDao.delete(it) }
    }
  }

  BetterSearchTheme(
    darkTheme = when (theme()) {
      1 -> false
      2 -> true
      else -> isSystemInDarkTheme()
    },
    dynamicColor = dynamicColor(),
  ) {
    NavHost(
      navController = navController,
      startDestination = if (introDone) Search else Intro,
      modifier = modifier,
    ) {
      composable<Search> {
        SearchScreen(
          query = queryValue,
          suggestions = suggestions,
          historySuggestions = historySuggestions,
          deleteEntry = { scope.launch { historyDao.delete(it) } },
          engine = engine,
          showPills = showPills(),
          pillsEngines = pillsEngines(),
          onSubmit = {
            scope.launch {
              startSearchIntent(context, it, engine())
              historyDao.insert(HistoryEntry(engine().id, it, System.currentTimeMillis()))
            }
          },
          navigateToSettings = { navController.navigate(Settings) { it() } },
        )
      }
      composable<Settings> {
        SettingsScreen(
          engine = engine,
          showPills = showPills,
          pillsEngines = pillsEngines,
          suggestHistory = suggestHistory,
          suggestHistoryAllEngines = suggestHistoryAllEngines,
          theme = theme,
          dynamicColor = dynamicColor,
          navigateToIntro = { navController.navigate(Intro) },
          navigateUp = navController::navigateUp,
        )
      }
      composable<Intro> {
        IntroScreen(
          saveIntroDone = { scope.launch { context.saveIntroDone(it) } },
          addWidget = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              val manager = AppWidgetManager.getInstance(context)
              val provider = ComponentName(context, BetterSearchWidgetReceiver::class.java)
              if (manager.isRequestPinAppWidgetSupported) {
                manager.requestPinAppWidget(provider, null, null)
              }
            }
          },
          navigateToSearch = { navController.navigate(Search) { popUpTo(0) { inclusive = true } } },
        )
      }
    }
  }
}

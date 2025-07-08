package ch.frauenfelderflorian.bettersearch.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.frauenfelderflorian.bettersearch.models.getSearchEngine
import ch.frauenfelderflorian.bettersearch.models.searchEngines
import ch.frauenfelderflorian.bettersearch.services.Prefs
import ch.frauenfelderflorian.bettersearch.services.dynamicColorFlow
import ch.frauenfelderflorian.bettersearch.services.fetchSuggestions
import ch.frauenfelderflorian.bettersearch.services.isFuzzyMatch
import ch.frauenfelderflorian.bettersearch.services.pillsEnginesFlow
import ch.frauenfelderflorian.bettersearch.services.room.HistoryDao
import ch.frauenfelderflorian.bettersearch.services.room.HistoryEntry
import ch.frauenfelderflorian.bettersearch.services.saveDynamicColor
import ch.frauenfelderflorian.bettersearch.services.saveIntroDone
import ch.frauenfelderflorian.bettersearch.services.savePillsEngines
import ch.frauenfelderflorian.bettersearch.services.saveSearchEngine
import ch.frauenfelderflorian.bettersearch.services.saveShowPills
import ch.frauenfelderflorian.bettersearch.services.saveSuggestHistory
import ch.frauenfelderflorian.bettersearch.services.saveSuggestHistoryAllEngines
import ch.frauenfelderflorian.bettersearch.services.saveTheme
import ch.frauenfelderflorian.bettersearch.services.searchEngineFlow
import ch.frauenfelderflorian.bettersearch.services.showPillsFlow
import ch.frauenfelderflorian.bettersearch.services.startSearchIntent
import ch.frauenfelderflorian.bettersearch.services.suggestHistoryAllEnginesFlow
import ch.frauenfelderflorian.bettersearch.services.suggestHistoryFlow
import ch.frauenfelderflorian.bettersearch.services.themeFlow
import ch.frauenfelderflorian.bettersearch.ui.screens.HistoryScreen
import ch.frauenfelderflorian.bettersearch.ui.screens.IntroScreen
import ch.frauenfelderflorian.bettersearch.ui.screens.SearchScreen
import ch.frauenfelderflorian.bettersearch.ui.screens.SettingsScreen
import ch.frauenfelderflorian.bettersearch.ui.theme.BetterSearchTheme
import ch.frauenfelderflorian.bettersearch.widget.BetterSearchWidgetReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun BetterSearchApp(
  historyDao: HistoryDao,
  introDone: Boolean,
  modifier: Modifier = Modifier,
  query: String = "",
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val navController = rememberNavController()

  val engine = context.searchEngineFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SEARCH_ENGINE,
  ).value.getSearchEngine()
  val showPills by context.showPillsFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_PILLS,
  )
  val pillsEngines = context.pillsEnginesFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.PILLS_ENGINES,
  ).value.map { it.getSearchEngine() }
  val suggestHistory by context.suggestHistoryFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SUGGEST_HISTORY,
  )
  val suggestHistoryAllEngines by context.suggestHistoryAllEnginesFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SUGGEST_HISTORY_ALL_ENGINES,
  )
  val theme by context.themeFlow.collectAsStateWithLifecycle(initialValue = Prefs.Defaults.THEME)
  val dynamicColor by context.dynamicColorFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.DYNAMIC_COLOR,
  )

  val historyAll by historyDao.getAll().collectAsStateWithLifecycle(initialValue = emptyList())
  val historyEngine by historyDao.getAllFromEngine(engine.id)
    .collectAsStateWithLifecycle(initialValue = emptyList())
  val history by remember(suggestHistoryAllEngines) {
    derivedStateOf { if (suggestHistoryAllEngines) historyAll else historyEngine }
  }

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
  val suggestions by produceState(
    initialValue = emptyList<String>(),
    key1 = queryValue.text,
    key2 = engine,
  ) {
    delay(100)
    value = fetchSuggestions(queryValue.text.toString(), engine)
  }
  val historySuggestions by remember(
    key1 = queryValue.text,
    key2 = history,
    key3 = suggestHistory,
  ) {
    derivedStateOf {
      if (queryValue.text.isNotBlank() && suggestHistory) {
        history.sortedByDescending { it.time }.filter {
          it.query.startsWith(queryValue.text.toString()) || isFuzzyMatch(
            queryValue.text.toString(), it.query
          )
        }
      } else if (suggestHistory) {
        history.sortedByDescending { it.time }.take(5)
      } else {
        emptyList()
      }
    }
  }

  // Delete the 10 oldest entries from the most-used engine if there are more than 10000 in total
  LaunchedEffect(historyAll.size) {
    if (historyAll.size > 10000) {
      searchEngines.map { historyDao.getAllFromEngine(it.id).first() }.maxByOrNull { it.size }
        ?.take(10)?.forEach { historyDao.delete(it) }
    }
  }

  BetterSearchTheme(
    darkTheme = when (theme) {
      1 -> false
      2 -> true
      else -> isSystemInDarkTheme()
    },
    dynamicColor = dynamicColor,
  ) {
    NavHost(
      navController = navController,
      startDestination = if (introDone) Search else Intro,
      enterTransition = { slideIn { IntOffset(it.width, 0) } },
      exitTransition = { slideOut { IntOffset(-it.width, 0) } },
      popEnterTransition = { slideIn { IntOffset(-it.width, 0) } },
      popExitTransition = { slideOut { IntOffset(it.width, 0) } },
      modifier = modifier,
    ) {
      composable<Search> {
        SearchScreen(
          query = queryValue,
          suggestions = suggestions,
          historySuggestions = historySuggestions,
          deleteEntry = { scope.launch { historyDao.delete(it) } },
          engine = engine,
          saveEngine = { scope.launch { context.saveSearchEngine(it) } },
          showPills = showPills,
          pillsEngines = pillsEngines,
          onSubmit = {
            startSearchIntent(context, it, engine)
            scope.launch {
              historyDao.insert(HistoryEntry(engine.id, it, System.currentTimeMillis()))
            }
          },
          navigateToSettings = { navController.navigate(Settings) { it() } },
          navigateToHistory = { navController.navigate(History) { it() } },
        )
      }
      composable<Settings> {
        SettingsScreen(
          engine = engine,
          saveEngine = { scope.launch { context.saveSearchEngine(it) } },
          showPills = showPills,
          saveShowPills = { scope.launch { context.saveShowPills(it) } },
          pillsEngines = pillsEngines,
          savePillsEngines = { scope.launch { context.savePillsEngines(it) } },
          suggestHistory = suggestHistory,
          saveSuggestHistory = { scope.launch { context.saveSuggestHistory(it) } },
          suggestHistoryAllEngines = suggestHistoryAllEngines,
          saveSuggestHistoryAllEngines = {
            scope.launch { context.saveSuggestHistoryAllEngines(it) }
          },
          theme = theme,
          saveTheme = { scope.launch { context.saveTheme(it) } },
          dynamicColor = dynamicColor,
          saveDynamicColor = { scope.launch { context.saveDynamicColor(it) } },
          navigateToIntro = { navController.navigate(Intro) },
          navigateToHistory = { navController.navigate(History) },
          navigateUp = navController::navigateUp,
        )
      }
      composable<History> {
        HistoryScreen(
          history = history,
          searchEntry = {
            startSearchIntent(context, it, engine)
            scope.launch {
              historyDao.insert(HistoryEntry(engine.id, it, System.currentTimeMillis()))
            }
          },
          deleteEntry = { scope.launch { historyDao.delete(it) } },
          navigateUp = { navController.navigateUp() },
        )
      }
      composable<Intro> {
        IntroScreen(
          saveIntroDone = { scope.launch { context.saveIntroDone(it) } },
          addWidget = {
            val manager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, BetterSearchWidgetReceiver::class.java)
            if (manager.isRequestPinAppWidgetSupported) {
              manager.requestPinAppWidget(provider, null, null)
            }
          },
          navigateToSearch = { navController.navigate(Search) { popUpTo(0) { inclusive = true } } },
        )
      }
    }
  }
}

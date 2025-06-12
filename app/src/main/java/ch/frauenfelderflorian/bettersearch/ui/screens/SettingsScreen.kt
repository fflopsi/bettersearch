package ch.frauenfelderflorian.bettersearch.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.frauenfelderflorian.bettersearch.R
import ch.frauenfelderflorian.bettersearch.models.SearchEngine
import ch.frauenfelderflorian.bettersearch.models.searchEngines
import ch.frauenfelderflorian.bettersearch.ui.components.InfoButton
import ch.frauenfelderflorian.bettersearch.ui.components.InfoDialog
import ch.frauenfelderflorian.bettersearch.ui.components.PillsEnginesSelectorDialog
import ch.frauenfelderflorian.bettersearch.ui.components.SettingsRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  engine: SearchEngine,
  saveEngine: (SearchEngine) -> Unit,
  showPills: Boolean,
  saveShowPills: (Boolean) -> Unit,
  pillsEngines: List<SearchEngine>,
  savePillsEngines: (List<SearchEngine>) -> Unit,
  suggestHistory: Boolean,
  saveSuggestHistory: (Boolean) -> Unit,
  suggestHistoryAllEngines: Boolean,
  saveSuggestHistoryAllEngines: (Boolean) -> Unit,
  theme: Int,
  saveTheme: (Int) -> Unit,
  dynamicColor: Boolean,
  saveDynamicColor: (Boolean) -> Unit,
  navigateToIntro: () -> Unit,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val showInfo = remember { mutableStateOf(false) }
  val showPillsEnginesSelector = remember { mutableStateOf(false) }
  var searchEngineSelectorExpanded by remember { mutableStateOf(false) }
  var themeSelectorExpanded by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(R.string.settings)) },
        navigationIcon = {
          IconButton(onClick = navigateUp) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
          }
        },
        actions = { InfoButton(show = showInfo) },
      )
    },
    modifier = modifier,
  ) { innerPadding ->
    InfoDialog(show = showInfo)
    PillsEnginesSelectorDialog(
      show = showPillsEnginesSelector,
      pillsEngines = pillsEngines,
      savePillsEngines = savePillsEngines,
    )

    Column(
      modifier = Modifier
        .consumeWindowInsets(innerPadding)
        .padding(innerPadding)
        .fillMaxWidth(),
    ) {
      SettingsRow(
        title = stringResource(R.string.search_engine),
        subtitle = engine.name,
        onClick = { searchEngineSelectorExpanded = true },
      ) {
        Box {
          Icon(Icons.Default.MoreVert, null)
          DropdownMenu(
            expanded = searchEngineSelectorExpanded,
            onDismissRequest = { searchEngineSelectorExpanded = false },
          ) {
            searchEngines.map {
              DropdownMenuItem(
                text = { Text(text = it.name) },
                onClick = {
                  searchEngineSelectorExpanded = false
                  saveEngine(it)
                },
                trailingIcon = { if (it.id == engine.id) Icon(Icons.Default.Check, null) },
              )
            }
          }
        }
      }
      SettingsRow(
        title = stringResource(R.string.show_quick_engines),
        subtitle = stringResource(R.string.show_quick_engines_desc),
        onClick = { saveShowPills(!showPills) },
        modifier = Modifier.height(IntrinsicSize.Min),
      ) {
        Switch(checked = showPills, onCheckedChange = null)
      }
      SettingsRow(
        title = stringResource(R.string.select_quick_engines),
        subtitle = pillsEngines.map { it.name }.ifEmpty { stringResource(R.string.none_selected) }
          .toString(),
        // Instead of AnimatedVisibility
        enabled = showPills,
        onClick = { showPillsEnginesSelector.value = true },
      ) {
        Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
      }
      SettingsRow(
        title = stringResource(R.string.suggest_from_history),
        subtitle = stringResource(R.string.suggest_from_history_desc),
        onClick = { saveSuggestHistory(!suggestHistory) },
      ) {
        Switch(checked = suggestHistory, onCheckedChange = null)
      }
      SettingsRow(
        title = stringResource(R.string.suggest_from_all_engines_history),
        subtitle = stringResource(R.string.suggest_from_all_engines_history_desc),
        enabled = suggestHistory,
        onClick = { saveSuggestHistoryAllEngines(!suggestHistoryAllEngines) },
      ) {
        Switch(checked = suggestHistoryAllEngines, onCheckedChange = null)
      }
      HorizontalDivider()
      SettingsRow(
        title = stringResource(R.string.theme),
        subtitle = stringResource(
          when (theme) {
            1 -> R.string.light
            2 -> R.string.dark
            else -> R.string.auto
          }
        ),
        onClick = { themeSelectorExpanded = true },
      ) {
        Box {
          Icon(Icons.Default.MoreVert, null)
          DropdownMenu(
            expanded = themeSelectorExpanded,
            onDismissRequest = { themeSelectorExpanded = false },
          ) {
            DropdownMenuItem(
              text = { Text(text = stringResource(R.string.auto)) },
              onClick = { saveTheme(0) },
              leadingIcon = { Icon(Icons.Default.BrightnessAuto, null) },
              trailingIcon = {
                if (theme == 0) Icon(Icons.Default.Check, stringResource(R.string.active))
              },
            )
            HorizontalDivider()
            DropdownMenuItem(
              text = { Text(text = stringResource(R.string.light)) },
              onClick = { saveTheme(1) },
              leadingIcon = { Icon(Icons.Default.LightMode, null) },
              trailingIcon = {
                if (theme == 1) Icon(Icons.Default.Check, stringResource(R.string.active))
              },
            )
            DropdownMenuItem(
              text = { Text(text = stringResource(R.string.dark)) },
              onClick = { saveTheme(2) },
              leadingIcon = { Icon(Icons.Default.DarkMode, null) },
              trailingIcon = {
                if (theme == 2) Icon(Icons.Default.Check, stringResource(R.string.active))
              },
            )
          }
        }
      }
      SettingsRow(
        title = stringResource(R.string.use_dynamic_colors),
        onClick = { saveDynamicColor(!dynamicColor) },
      ) {
        Switch(checked = dynamicColor, onCheckedChange = null)
      }
      HorizontalDivider()
      SettingsRow(
        title = stringResource(R.string.show_intro),
        onClick = navigateToIntro,
      ) {
        Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
      }
    }
  }
}

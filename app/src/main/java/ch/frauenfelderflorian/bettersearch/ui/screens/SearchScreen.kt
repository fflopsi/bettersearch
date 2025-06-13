package ch.frauenfelderflorian.bettersearch.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ch.frauenfelderflorian.bettersearch.R
import ch.frauenfelderflorian.bettersearch.models.SearchEngine
import ch.frauenfelderflorian.bettersearch.services.Setting
import ch.frauenfelderflorian.bettersearch.services.room.HistoryEntry
import ch.frauenfelderflorian.bettersearch.ui.components.InfoButton
import ch.frauenfelderflorian.bettersearch.ui.components.InfoDialog
import ch.frauenfelderflorian.bettersearch.ui.components.SuggestionRow

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
  query: TextFieldState,
  suggestions: List<String>,
  historySuggestions: List<HistoryEntry>,
  deleteEntry: (HistoryEntry) -> Unit,
  engine: Setting<SearchEngine>,
  showPills: Boolean,
  pillsEngines: List<SearchEngine>,
  onSubmit: (String) -> Unit,
  navigateToSettings: (() -> Unit) -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  val showInfo = remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        subtitle = { Text(text = engine().name) },
        actions = {
          Row {
            InfoButton(show = showInfo)
            IconButton(onClick = { navigateToSettings { keyboardController?.hide() } }) {
              Icon(Icons.Default.Settings, stringResource(R.string.settings))
            }
          }
        },
      )
    },
    modifier = modifier,
  ) { innerPadding ->
    InfoDialog(show = showInfo)

    Column(
      modifier = Modifier
        .consumeWindowInsets(innerPadding)
        .padding(innerPadding),
    ) {
      if (showPills && pillsEngines.isNotEmpty()) {
        LazyRow(contentPadding = PaddingValues(12.dp)) {
          items(pillsEngines) {
            FilterChip(
              label = { Text(text = it.name) },
              selected = engine().id == it.id,
              onClick = { engine(it) },
              modifier = Modifier.padding(horizontal = 4.dp),
            )
          }
        }
      }
      OutlinedTextField(
        state = query,
        label = { Text(text = stringResource(R.string.search)) },
        placeholder = { Text(text = stringResource(R.string.search_in, engine().name)) },
        lineLimits = TextFieldLineLimits.SingleLine,
        trailingIcon = {
          Row {
            if (query.text.isNotBlank()) {
              IconButton(onClick = { query.clearText() }) {
                Icon(Icons.Default.Clear, stringResource(R.string.clear))
              }
            }
            IconButton(
              onClick = { if (query.text.isNotBlank()) onSubmit(query.text.toString()) },
              enabled = query.text.isNotBlank(),
            ) {
              Icon(Icons.Default.Search, stringResource(R.string.search))
            }
          }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = if (query.text.isNotBlank()) ImeAction.Search else ImeAction.Unspecified,
        ),
        onKeyboardAction = {
          if (query.text.isNotBlank()) {
            onSubmit(query.text.toString())
          } else {
            keyboardController?.hide()
          }
        },
        modifier = Modifier
          .focusRequester(focusRequester)
          .padding(16.dp)
          .fillMaxWidth(),
      )
      LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(historySuggestions) {
          SuggestionRow(
            entry = it,
            queryState = query,
            onDelete = { deleteEntry(it) },
            onSubmit = { onSubmit(it.query) },
          )
        }
        items(suggestions) {
          SuggestionRow(text = it, queryState = query, onSubmit = { onSubmit(it) })
        }
      }
    }
  }
}

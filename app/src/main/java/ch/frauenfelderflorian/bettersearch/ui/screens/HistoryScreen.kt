package ch.frauenfelderflorian.bettersearch.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.frauenfelderflorian.bettersearch.R
import ch.frauenfelderflorian.bettersearch.models.SearchEngine
import ch.frauenfelderflorian.bettersearch.models.getSearchEngine
import ch.frauenfelderflorian.bettersearch.models.searchEngines
import ch.frauenfelderflorian.bettersearch.services.room.HistoryEntry
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun HistoryScreen(
  history: List<HistoryEntry>,
  searchEntry: (String) -> Unit,
  deleteEntry: (HistoryEntry) -> Unit,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  var searchEngineSelectorExpanded by remember { mutableStateOf(false) }
  var selectedSearchEngine: SearchEngine? by remember { mutableStateOf(null) }
  val selectedSearchEngineText by remember {
    derivedStateOf { TextFieldState(selectedSearchEngine?.name ?: context.getString(R.string.all)) }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(R.string.search_history)) },
        navigationIcon = {
          IconButton(onClick = navigateUp) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
          }
        },
      )
    },
    modifier = modifier,
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .consumeWindowInsets(innerPadding)
        .padding(innerPadding)
        .fillMaxWidth(),
    ) {
      ExposedDropdownMenuBox(
        expanded = searchEngineSelectorExpanded,
        onExpandedChange = { searchEngineSelectorExpanded = it },
        modifier = Modifier.padding(16.dp),
      ) {
        TextField(
          state = selectedSearchEngineText,
          label = { Text(text = stringResource(R.string.search_engine)) },
          readOnly = true,
          lineLimits = TextFieldLineLimits.SingleLine,
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = searchEngineSelectorExpanded)
          },
          colors = ExposedDropdownMenuDefaults.textFieldColors(),
          modifier = Modifier
            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth(),
        )
        ExposedDropdownMenu(
          expanded = searchEngineSelectorExpanded,
          onDismissRequest = { searchEngineSelectorExpanded = false },
        ) {
          DropdownMenuItem(
            text = { Text(text = stringResource(R.string.all)) },
            onClick = {
              selectedSearchEngine = null
              searchEngineSelectorExpanded = false
            },
            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
          )
          searchEngines.forEach {
            DropdownMenuItem(
              text = { Text(text = it.name) },
              onClick = {
                selectedSearchEngine = it
                searchEngineSelectorExpanded = false
              },
              contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
          }
        }
      }
      LazyColumn(modifier = Modifier.fillMaxHeight()) {
        item(key = 0) {
          AnimatedVisibility(
            visible = selectedSearchEngine?.run { history.none { this.id == it.engineId } }
              ?: history.isEmpty(),
            modifier = Modifier.animateItem(),
          ) {
            Text(
              text = stringResource(R.string.search_history_empty),
              fontStyle = FontStyle.Italic,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }
        items(
          items = history.sortedByDescending { it.time },
          key = { it.time },
        ) {
          AnimatedVisibility(
            visible = selectedSearchEngine?.run { id == it.engineId } != false,
            modifier = Modifier.animateItem(),
          ) {
            var expanded by remember { mutableStateOf(false) }

            val searchTime = Instant.fromEpochMilliseconds(it.time)
            val formattedTime = remember {
              DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.current.platformLocale).format(
                  searchTime.toJavaInstant().atZone(ZoneId.systemDefault())
                )
            }

            Column(
              modifier = Modifier
                .clickable(onClick = { expanded = !expanded })
                .padding(vertical = 4.dp)
                .padding(start = 32.dp, end = 16.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = it.query,
                  maxLines = if (expanded) Int.MAX_VALUE else 2,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier
                    .weight(1f)
                    .animateContentSize(),
                )
                val duration = Clock.System.now() - searchTime
                Text(
                  text = when {
                    duration < 1.minutes -> stringResource(R.string.just_now)
                    duration < 1.hours -> pluralStringResource(
                      id = R.plurals.minutes_ago,
                      count = duration.inWholeMinutes.toInt(),
                      duration.inWholeMinutes
                    )

                    duration < 1.days -> pluralStringResource(
                      id = R.plurals.hours_ago,
                      count = duration.inWholeHours.toInt(),
                      duration.inWholeHours
                    )

                    duration < 8.days -> pluralStringResource(
                      id = R.plurals.days_ago,
                      count = duration.inWholeDays.toInt(),
                      duration.inWholeDays
                    )

                    else -> formattedTime
                  },
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.padding(horizontal = 16.dp),
                )
                IconButton(onClick = { searchEntry(it.query) }) {
                  Icon(Icons.AutoMirrored.Filled.OpenInNew, stringResource(R.string.search))
                }
                IconButton(onClick = { deleteEntry(it) }) {
                  Icon(Icons.Default.Delete, stringResource(R.string.delete))
                }
              }
              AnimatedVisibility(visible = expanded) {
                Text(
                  text = stringResource(
                    R.string.at_time_with_engine, formattedTime, it.engineId.getSearchEngine().name
                  ),
                  style = MaterialTheme.typography.bodySmall,
                )
              }
            }
          }
        }
      }
    }
  }
}

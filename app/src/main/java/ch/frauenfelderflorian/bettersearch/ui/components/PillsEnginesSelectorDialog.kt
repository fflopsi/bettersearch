package ch.frauenfelderflorian.bettersearch.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditAttributes
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import ch.frauenfelderflorian.bettersearch.R
import ch.frauenfelderflorian.bettersearch.models.SearchEngine
import ch.frauenfelderflorian.bettersearch.models.searchEngines

@Composable
fun PillsEnginesSelectorDialog(
  show: MutableState<Boolean>,
  pillsEngines: List<SearchEngine>,
  savePillsEngines: (List<SearchEngine>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val pills = pillsEngines.toMutableStateList()

  if (show.value) {
    AlertDialog(
      onDismissRequest = { show.value = false },
      confirmButton = {
        TextButton(
          onClick = {
            show.value = false
            if (pills.toList() != pillsEngines) {
              // Necessary because saving the same list modified in order does NOT save the list
              savePillsEngines(emptyList())
              savePillsEngines(pills)
            }
          },
        ) {
          Text(text = stringResource(R.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = { show.value = false }) {
          Text(text = stringResource(R.string.cancel))
        }
      },
      icon = { Icon(Icons.Default.EditAttributes, null) },
      title = { Text(text = stringResource(R.string.select_quick_engines)) },
      text = {
        LazyColumn {
          itemsIndexed(pills) { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(text = item.name, modifier = Modifier.weight(1f))
              if (index < pills.lastIndex) {
                IconButton(onClick = { pills.add(index + 1, pills.removeAt(index)) }) {
                  Icon(Icons.Default.KeyboardArrowDown, stringResource(R.string.move_down))
                }
              }
              if (index > 0) {
                IconButton(onClick = { pills.add(index - 1, pills.removeAt(index)) }) {
                  Icon(Icons.Default.KeyboardArrowUp, stringResource(R.string.move_up))
                }
              }
              IconButton(onClick = { pills.removeAt(index) }) {
                Icon(Icons.Default.Remove, stringResource(R.string.remove))
              }
            }
          }
          if ((searchEngines - pills).isNotEmpty()) {
            item {
              Column {
                HorizontalDivider()
                Text(
                  text = stringResource(R.string.available_search_engines),
                  fontStyle = FontStyle.Italic,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            }
          }
          items(searchEngines - pills) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(text = it.name, modifier = Modifier.weight(1f))
              IconButton(onClick = { pills.add(it) }) {
                Icon(Icons.Default.Add, stringResource(R.string.add))
              }
            }
          }
        }
      },
      modifier = modifier,
    )
  }
}

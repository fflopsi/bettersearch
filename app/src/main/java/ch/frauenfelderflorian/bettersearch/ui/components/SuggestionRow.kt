package ch.frauenfelderflorian.bettersearch.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.frauenfelderflorian.bettersearch.R
import ch.frauenfelderflorian.bettersearch.services.room.HistoryEntry

@Composable
fun SuggestionRow(
  text: String,
  queryState: TextFieldState,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clickable(onClick = onSubmit)
      .padding(start = 32.dp, end = 16.dp),
  ) {
    Text(
      text = text,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f),
    )
    IconButton(onClick = { queryState.setTextAndPlaceCursorAtEnd(text) }) {
      Icon(Icons.Default.NorthWest, stringResource(R.string.fill))
    }
  }
}

@Composable
fun SuggestionRow(
  entry: HistoryEntry,
  queryState: TextFieldState,
  onDelete: () -> Unit,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clickable(onClick = onSubmit)
      .padding(start = 32.dp, end = 16.dp),
  ) {
    Icon(
      imageVector = Icons.Default.AccessTime,
      contentDescription = stringResource(R.string.from_history),
      modifier = Modifier.padding(end = 8.dp),
    )
    Text(
      text = entry.query,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f),
    )
    IconButton(onClick = onDelete) {
      Icon(Icons.Default.Delete, stringResource(R.string.delete))
    }
    IconButton(onClick = { queryState.setTextAndPlaceCursorAtEnd(entry.query) }) {
      Icon(Icons.Default.NorthWest, stringResource(R.string.fill))
    }
  }
}

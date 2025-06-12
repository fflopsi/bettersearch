package ch.frauenfelderflorian.bettersearch.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import ch.frauenfelderflorian.bettersearch.BuildConfig
import ch.frauenfelderflorian.bettersearch.R

@Composable
fun InfoButton(
  show: MutableState<Boolean>,
  modifier: Modifier = Modifier,
) {
  IconButton(
    onClick = { show.value = true },
    modifier = modifier,
  ) {
    Icon(Icons.Default.Info, stringResource(R.string.about))
  }
}

@Composable
fun InfoDialog(
  show: MutableState<Boolean>,
  modifier: Modifier = Modifier,
) {
  if (show.value) AlertDialog(
    onDismissRequest = { show.value = false },
    icon = { Icon(Icons.Default.Info, null) },
    title = { Text(text = stringResource(R.string.app_name)) },
    text = {
      Text(
        text = buildAnnotatedString {
          append(stringResource(R.string.built_by))
          append("\n")
          withLink(
            LinkAnnotation.Url(
              stringResource(R.string.github_link),
              TextLinkStyles(
                style = SpanStyle(
                  color = MaterialTheme.colorScheme.tertiary,
                  textDecoration = TextDecoration.Underline,
                ),
              ),
            )
          ) {
            append(stringResource(R.string.github_link))
          }
          append("\n")
          append(stringResource(R.string.version, BuildConfig.VERSION_NAME))
        },
        textAlign = TextAlign.Center,
      )
    },
    confirmButton = {
      TextButton(onClick = { show.value = false }) { Text(text = stringResource(R.string.ok)) }
    },
    modifier = modifier,
  )
}

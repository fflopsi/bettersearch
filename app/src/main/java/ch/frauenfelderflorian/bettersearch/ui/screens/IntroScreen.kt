package ch.frauenfelderflorian.bettersearch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ch.frauenfelderflorian.bettersearch.R
import ch.frauenfelderflorian.bettersearch.ui.components.InfoDialog
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(
  saveIntroDone: (Boolean) -> Unit,
  addWidget: () -> Unit,
  navigateToSearch: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val showInfo = remember { mutableStateOf(false) }
  var showIntroAgain by rememberSaveable { mutableStateOf(false) }

  Scaffold(
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = {
          navigateToSearch()
          saveIntroDone(!showIntroAgain)
        },
        icon = { Icon(Icons.Default.Done, null) },
        text = { Text(text = stringResource(R.string.done)) },
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
      val scrollState = rememberScrollState()
      LaunchedEffect(Unit) {
        delay(1000)
        scrollState.animateScrollTo(100)
      }
      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(scrollState)
          .padding(16.dp),
      ) {
        Text(
          text = stringResource(R.string.thank_you),
          style = MaterialTheme.typography.headlineLarge,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
          text = stringResource(R.string.app_desc),
          style = MaterialTheme.typography.bodyLarge,
          fontStyle = FontStyle.Italic,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
          text = buildAnnotatedString {
            append(stringResource(R.string.search_bar_instruction))
            append("\n")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
              append(stringResource(R.string.bad_app_examples))
            }
          },
          style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = stringResource(R.string.restriction_icons))
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = stringResource(R.string.app_icon_widget_desc))
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(
          onClick = addWidget,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
          Text(text = stringResource(R.string.add_widget))
        }
      }
      Column(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showIntroAgain = !showIntroAgain }
            .padding(16.dp),
        ) {
          Checkbox(checked = showIntroAgain, onCheckedChange = { showIntroAgain = !showIntroAgain })
          Text(text = stringResource(R.string.show_intro_again))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = stringResource(
            R.string.show_intro_again_settings, stringResource(R.string.show_intro)
          ),
          style = MaterialTheme.typography.bodyMedium,
          fontStyle = FontStyle.Italic,
          modifier = Modifier.padding(16.dp),
        )
        Spacer(modifier = Modifier.height(80.dp))
      }
    }
  }
}

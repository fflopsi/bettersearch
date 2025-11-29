package ch.frauenfelderflorian.bettersearch.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import ch.frauenfelderflorian.bettersearch.MainActivity
import ch.frauenfelderflorian.bettersearch.R
import ch.frauenfelderflorian.bettersearch.models.getSearchEngine
import ch.frauenfelderflorian.bettersearch.services.Prefs
import ch.frauenfelderflorian.bettersearch.services.searchEngineFlow

class BetterSearchWidget : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
      val engine =
        context.searchEngineFlow.collectAsState(initial = Prefs.Defaults.SEARCH_ENGINE)
          .value.getSearchEngine()
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier
          .clickable(actionStartActivity<MainActivity>())
          .padding(16.dp)
          .fillMaxWidth()
          .height(64.dp)
          .background(GlanceTheme.colors.widgetBackground),
      ) {
        CircleIconButton(
          onClick = actionStartActivity<MainActivity>(),
          imageProvider = ImageProvider(R.drawable.search),
          contentDescription = null,
          backgroundColor = null,
          modifier = GlanceModifier.padding(end = 8.dp),
        )
        Text(
          text = context.applicationContext.resources.getString(R.string.search_in, engine.name),
          maxLines = 1,
          style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 16.sp),
          modifier = GlanceModifier.defaultWeight(),
        )
//          CircleIconButton(
//            onClick = actionStartActivity<MainActivity>(
//              parameters = actionParametersOf(
//                ActionParameters.Key<String>("navigateTo") to "settings",
//              ),
//            ),
//            imageProvider = ImageProvider(R.drawable.settings),
//            contentDescription = null,
//            backgroundColor = null,
//          )
      }
    }
  }
}

package ch.frauenfelderflorian.bettersearch.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class BetterSearchWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget = BetterSearchWidget()
}

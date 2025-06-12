package ch.frauenfelderflorian.bettersearch.services.room

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "history", primaryKeys = ["engineId", "query"])
data class HistoryEntry(
  val engineId: UUID,
  val query: String,
  val time: Long,
) : Parcelable

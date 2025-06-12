package ch.frauenfelderflorian.bettersearch.services.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntry::class], version = 1)
abstract class BetterSearchDatabase : RoomDatabase() {
  abstract fun historyDao(): HistoryDao
}

package ch.frauenfelderflorian.bettersearch.services.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface HistoryDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(entry: HistoryEntry)

  @Delete
  suspend fun delete(entry: HistoryEntry)

  @Query("SELECT * FROM history")
  fun getAll(): Flow<List<HistoryEntry>>

  @Query("SELECT * FROM history WHERE engineId = :engineId")
  fun getAllFromEngine(engineId: UUID): Flow<List<HistoryEntry>>

//  @Query("SELECT * FROM history WHERE `query` LIKE :search")
//  fun getAllLikeQuery(search: String): Flow<List<HistoryEntry>>

//  @Query("SELECT * FROM history WHERE engineId = :engineId AND `query` LIKE :search")
//  fun getAllFromEngineLikeQuery(engineId: UUID, search: String): Flow<List<HistoryEntry>>
}

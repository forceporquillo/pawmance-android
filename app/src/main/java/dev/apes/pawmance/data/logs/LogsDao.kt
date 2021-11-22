package dev.apes.pawmance.data.logs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogsDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun addLogs(logs: Logs)

  @Query("SELECT * FROM logs")
  fun getAllLogs(): Flow<List<Logs>>
}
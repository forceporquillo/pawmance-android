package dev.apes.pawmance.data.progress

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertProgress(petProgress: PetProgress)

  @Query("SELECT * FROM petprogress")
  fun getAllProgress(): Flow<List<PetProgress>>

  @Query("SELECT * FROM petprogress WHERE day=:day")
  fun getAllProgress(day: String): Flow<PetProgress?>
}
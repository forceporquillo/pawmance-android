package dev.apes.pawmance.data.logs

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity
data class Logs(

  val stack: String,
  val tag: String,
  val priority: Int,

  val timestamp: Long = System.currentTimeMillis(),

  @PrimaryKey(autoGenerate = true)
  val id: Int = 0
)


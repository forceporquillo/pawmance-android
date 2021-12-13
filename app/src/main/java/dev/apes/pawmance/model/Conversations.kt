package dev.apes.pawmance.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.apes.pawmance.utils.TimeUtils
import java.util.Calendar

@Entity
data class Conversations(
  val content: String,
  val petName: String,
  val petId: String,
  val timestamp: Long = Calendar.getInstance().timeInMillis,
  val fromSender: Boolean,
  val recipientId: String,

  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
)
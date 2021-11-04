package dev.forcecodes.pawmance.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.forcecodes.pawmance.utils.TimeUtils

@Entity
data class Conversations(
  val content: String,
  val petName: String,
  val petId: String,
  val timestamp: Long = TimeUtils.instantTimeLocalize(),
  val fromSender: Boolean,
  val recipientId: String,

  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
)
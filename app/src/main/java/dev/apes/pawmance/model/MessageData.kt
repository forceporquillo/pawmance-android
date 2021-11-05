package dev.apes.pawmance.model

import dev.apes.pawmance.utils.TimeUtils

data class MessageData(
  val content: String,
  val petName: String,
  val petId: String,

  val recipientId: String,
  val timestamp: Long = TimeUtils.instantTimeLocalize()
)

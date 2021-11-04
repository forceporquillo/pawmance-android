package dev.forcecodes.pawmance.model

data class PushMessage(
  val to: String,
  val data: MessageData
)
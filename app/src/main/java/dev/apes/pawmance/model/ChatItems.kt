package dev.apes.pawmance.model

data class ChatItems(
  val name: String,
  val profile: String,
  val recentMessage: String,
  val petId: String,
  val recipientId: String
)
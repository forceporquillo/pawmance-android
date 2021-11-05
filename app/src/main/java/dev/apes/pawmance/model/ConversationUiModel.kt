package dev.apes.pawmance.model

data class ConversationUiModel(
  val content: String,
  val petName: String,
  val petId: String,
  val fromSender: Boolean,
  val recipientId: String,
  val id: Int,
  val petPetProfile: String? = null
)

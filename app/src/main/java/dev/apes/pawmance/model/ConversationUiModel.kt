package dev.apes.pawmance.model



sealed class ConversationUiModel {
  open val content: String = ""
  open val petName: String = ""
  open val petId: String = ""
  open val petPetProfile: String? = null
  open val recipientId: String = ""
  open val id: Int = -1
  open val timestamp: String = ""

  data class ReceivedUiModel(
    override val content: String,
    override val petId: String,
    override val petName: String,
    override val petPetProfile: String?,
    override val id: Int,
    override val recipientId: String,
    override val timestamp: String
  ): ConversationUiModel()

  data class SenderUiModel(
    override val content: String,
    override val petId: String,
    override val petName: String,
    override val petPetProfile: String?,
    override val id: Int,
    override val recipientId: String,
    override val timestamp: String
  ): ConversationUiModel()

  data class DateHeaderUiModel(
    override val timestamp: String
  ): ConversationUiModel()
}
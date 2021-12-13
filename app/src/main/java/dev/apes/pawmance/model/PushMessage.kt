package dev.apes.pawmance.model

data class PushMessage(
  val to: String,
  val data: MessageDataV2
)

data class MessageDataV2(
  val matchName: String? = null,
  val youLike: String? = null
)
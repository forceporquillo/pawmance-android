package dev.forcecodes.pawmance.data.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.forcecodes.pawmance.db.AppDatabase
import dev.forcecodes.pawmance.model.Conversations
import dev.forcecodes.pawmance.utils.TimeUtils
import timber.log.Timber

class FirebaseMessageService : FirebaseMessagingService() {

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Timber.e("New firebase token: $token")
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    val conversations = remoteMessage.mapToConversation()

    AppDatabase.getInstance()
      .conversationDao()
      .insertMessage(conversations)

    Timber.e("Message received ${remoteMessage.senderId} content: ${remoteMessage.data["content"]}")
  }

  private fun RemoteMessage.mapToConversation(): Conversations {
    val content = data["content"] as String
    val petName = data["petName"] as String
    val petId = data["petId"] as String
    val timeStamp = data["timeStamp"]
    val recipientId = data["recipientId"] as String

    return Conversations(
      content,
      petName,
      recipientId,
      timeStamp?.toLong()
        ?: TimeUtils.instantTimeLocalize(),
      true,
      petId
    )
  }
}
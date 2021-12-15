package dev.apes.pawmance.data.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.data.conversation.ConversationsDao
import dev.apes.pawmance.model.Conversations
import io.karn.notify.Notify
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessageService : FirebaseMessagingService() {

  @Inject
  lateinit var conversationsDao: ConversationsDao

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Timber.e("New firebase token: $token")
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    remoteMessage.data["matchName"]?.let { name ->
      showMatchNotifications(
        header = {
          "Wow! You have a new match! ❤\uD83D\uDE0D"
        },
        matchName = {
          "You and $name both liked each other! \uD83D\uDC36"
        }
      )
    }

    remoteMessage.data["youLike"]?.let { name ->
      showMatchNotifications(
        header = {
          "Someone likes your profile! \uD83D\uDC4D"
        },
        matchName = {
          "$name likes you, Swipe to see now! ❤\uD83D\uDE0D"
        }
      )
    }
  }

  private inline fun showMatchNotifications(
    crossinline header: () -> String,
    crossinline matchName: () -> String
  ) {
    Notify
      .with(applicationContext)
      .content { // this: Payload.Content.Default
        // The title of the notification (first line).
        title = header.invoke()
        // The second line of the notification.
        text = matchName.invoke()
      }
      .show()
  }
}
package dev.apes.pawmance.initializer

import android.content.Context
import android.content.Intent
import androidx.startup.Initializer
import com.devforcecodes.pawmance.BuildConfig
import dev.apes.pawmance.ui.MainActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator
import timber.log.Timber

/**
 * StreamChatInitializer initializes all Stream Client components.
 */
class StreamChatInitializer : Initializer<Unit> {

  override fun create(context: Context) {
    Timber.d("StreamChatInitializer is initialized")

    /**
     * initialize a global instance of the [ChatClient].
     * The ChatClient is the main entry point for all low-level operations on chat.
     * e.g, connect/disconnect user to the server, send/update/pin message, etc.
     */
    val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING
    val chatClient: ChatClient =
      ChatClient.Builder("d4ur7bzhfjbz", context)
        .notifications(createNotificationConfig(), createNotificationHandler(context))
        .logLevel(logLevel)
        .build()

    /**
     * initialize a global instance of the [ChatDomain].
     * The ChatDomain is the main entry point for all livedata & offline operations on chat.
     * e.g, querying available channel lists, querying users, etc.
     */
    ChatDomain.Builder(chatClient, context)
      .offlineEnabled()
      .build()
  }

  /**
   * Creates [NotificationConfig] that configures push notifications.
   */
  private fun createNotificationConfig(): NotificationConfig {
    return NotificationConfig(
      pushDeviceGenerators = listOf(
        FirebasePushDeviceGenerator()
      )
    )
  }

  /**
   * Creates [NotificationHandler] that handles new push notifications and
   * customizes an intent the user triggers when clicking on a notification.
   */
  private fun createNotificationHandler(
    context: Context
  ): NotificationHandler {
    return NotificationHandlerFactory.createNotificationHandler(
      context = context,
      newMessageIntent = { _: String, _: String, _: String ->
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
      }
    )
  }

  override fun dependencies(): List<Class<out Initializer<*>>> =
    listOf(FirebaseInitializer::class.java)
}

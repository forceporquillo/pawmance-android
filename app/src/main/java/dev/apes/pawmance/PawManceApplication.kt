package dev.apes.pawmance

import android.app.Application
import android.content.Context
import com.devforcecodes.pawmance.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import dev.apes.pawmance.data.logs.Logs
import dev.apes.pawmance.data.logs.LogsDao
import dev.apes.pawmance.utils.TimeUtils.nowToFormattedDate
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.offline.ChatDomain
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltAndroidApp
class PawManceApplication : Application() {

  @Inject
  lateinit var logsDao: LogsDao

  override fun onCreate() {
    super.onCreate()
    AndroidThreeTen.init(this)

    if (BuildConfig.DEBUG) {
      Timber.plant(CrashlyticsTree(logsDao))
    }

    //AppInitializer.initializeChatDomain(this)
  }

  object AppInitializer {

    fun initializeChatDomain(context: Context) {
      Timber.e("StreamChatInitializer is initialized")

      val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING

      val chatClient = ChatClient.Builder("d4ur7bzhfjbz", context)
          .logLevel(logLevel)
          .build()

      ChatDomain
        .Builder(chatClient, context)
        .offlineEnabled()
        .build()
    }
  }

  internal class CrashlyticsTree(private val logsDao: LogsDao) : DebugTree() {
    private val executor = Executors.newSingleThreadExecutor()

    override fun log(
      priority: Int, tag: String?, message: String,
      t: Throwable?
    ) {
      super.log(priority, tag, message, t)

      val formattedDate = nowToFormattedDate()
      crashlytics.log("$formattedDate [$tag] $message")

      executor.execute {
        logsDao.addLogs(Logs(message, tag ?: "Unknown error.", priority))
      }
    }

    companion object {
      private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()
    }
  }
}
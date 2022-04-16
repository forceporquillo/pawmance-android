package dev.apes.pawmance.data.conversation

import androidx.annotation.WorkerThread
import dev.apes.pawmance.data.auth.PetInfo
import dev.apes.pawmance.di.IoDispatcher
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.onSuccessSuspend
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateChannelSource @Inject constructor(
  private val chatClient: ChatClient,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) {

  fun createChannel(theirId: String, ourId: String) = flow {
    val result = chatClient.createChannel("messaging", listOf(theirId, ourId)).await()
    result.onSuccessSuspend {
      emit(it)
    }
  }.flowOn(dispatcher)
}

interface StreamClientConnector {
  fun connectUser(userId: String, info: PetInfo): Flow<ConnectionData>
  fun disconnectUser(userId: String)
}

class StreamClientConnectorImpl @Inject constructor(
  private val chatClient: ChatClient,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : StreamClientConnector {

  @WorkerThread
  override fun connectUser(userId: String, info: PetInfo) = flow {
    disconnectUser(userId)

    val user = User(
      id = userId,
      extraData = info.extraData
    )

    try {
      val devToken = chatClient.devToken(userId)
      val result = chatClient.connectUser(user, devToken).await()

      result.onSuccessSuspend {
        Timber.e("Connection Data $it")
        emit(it)
      }

    } catch (e: IllegalStateException) {
      Timber.e(e)
    }

  }.flowOn(dispatcher)

  override fun disconnectUser(userId: String) {
    val currentUser = chatClient.getCurrentUser()
    if (currentUser != null && userId == currentUser.id) {
      chatClient.disconnect()
    }
  }
}

@PublishedApi
internal const val EXTRA_NAME = "name"

@PublishedApi
internal const val EXTRA_IMAGE = "image"

@PublishedApi
internal const val EXTRA_TEAM = "team"

val PetInfo.extraData: MutableMap<String, Any>
  inline get() = mutableMapOf(
    EXTRA_NAME to this.petName().orEmpty(),
    EXTRA_IMAGE to this.petPrimaryProfile().orEmpty()
  )

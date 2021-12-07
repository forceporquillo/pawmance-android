package dev.apes.pawmance.data

import dev.apes.pawmance.api.PushMessageNotifierApi
import dev.apes.pawmance.di.FcmMessageService
import dev.apes.pawmance.di.IoDispatcher
import dev.apes.pawmance.model.MessageDataV2
import dev.apes.pawmance.model.PushMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class PushNotificationManager @Inject constructor(
  @FcmMessageService private val pushMessageNotifierApi: PushMessageNotifierApi,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

  suspend fun notifyBreedMatch(tokenId: String, messageData: MessageDataV2) {
    withContext(ioDispatcher) {
      try {
        pushMessageNotifierApi.sendMessage(PushMessage(tokenId, messageData))
      } catch (e: Exception) {
        Timber.e(e)
      }
    }
  }
}
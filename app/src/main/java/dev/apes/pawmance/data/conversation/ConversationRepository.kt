package dev.apes.pawmance.data.conversation

import dev.apes.pawmance.api.PushMessageNotifierApi
import dev.apes.pawmance.di.FcmMessageService
import dev.apes.pawmance.di.IoDispatcher
import dev.apes.pawmance.model.Conversations
import dev.apes.pawmance.model.MessageData
import dev.apes.pawmance.model.PushMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

interface ConversationRepository {
  suspend fun sendMessage(messageData: MessageData, recipientToken: String, petId: String?)
  fun getConversations(petId: String): Flow<List<Conversations>>
  fun getAllConversations(): Flow<List<Conversations>>
}

class ConversationRepositoryImpl @Inject constructor(
  private val conversationsDao: ConversationsDao,
  @FcmMessageService private val pushMessageNotifierApi: PushMessageNotifierApi,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ConversationRepository {

  override suspend fun sendMessage(
    messageData: MessageData,
    recipientToken: String,
    petId: String?
  ) {
    insertMessage(messageData, petId)
    withContext(ioDispatcher) {
      try {
        //pushMessageNotifierApi.sendMessage(PushMessage(recipientToken, messageData))
      } catch (e: Exception) {
        Timber.e(e)
      }
    }
  }

  private suspend fun insertMessage(messageData: MessageData, petId: String?) {
    withContext(ioDispatcher) {
      val conversation = Conversations(
        messageData.content,
        messageData.petName,
        messageData.petId,
        fromSender = false,
        recipientId = petId!!,
      )

      conversationsDao.insertMessage(conversation)
    }
  }

  override fun getConversations(petId: String): Flow<List<Conversations>> {
    return conversationsDao.getConversation(petId)
  }

  override fun getAllConversations(): Flow<List<Conversations>> {
    return flow {
      emitAll(
        conversationsDao
          .getAllConversations()
          .map { list ->
            list.distinctBy { conversations ->
              conversations.petId
            }.sortedByDescending { conversations ->
              conversations.timestamp
            }.distinct()
          }
      )
    }
  }
}
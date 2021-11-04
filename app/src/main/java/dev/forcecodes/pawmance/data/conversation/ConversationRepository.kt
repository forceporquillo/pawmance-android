package dev.forcecodes.pawmance.data.conversation

import dev.forcecodes.pawmance.api.PushMessageNotifierApi
import dev.forcecodes.pawmance.db.AppDatabase
import dev.forcecodes.pawmance.di.FcmMessageService
import dev.forcecodes.pawmance.di.IoDispatcher
import dev.forcecodes.pawmance.model.Conversations
import dev.forcecodes.pawmance.model.MessageData
import dev.forcecodes.pawmance.model.PushMessage
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
        pushMessageNotifierApi.sendMessage(PushMessage(recipientToken, messageData))
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
        messageData.timestamp,
        false,
        petId!!,
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
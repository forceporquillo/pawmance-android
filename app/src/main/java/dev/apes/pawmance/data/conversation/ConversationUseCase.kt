package dev.apes.pawmance.data.conversation

import dev.apes.pawmance.data.FlowUseCase
import dev.apes.pawmance.data.auth.PetInfoStateDataSource
import dev.apes.pawmance.di.IoDispatcher
import dev.apes.pawmance.model.ChatItems
import dev.apes.pawmance.utils.Result
import dev.apes.pawmance.utils.data
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConversationUseCase @Inject constructor(
  private val conversationRepository: ConversationRepository,
  private val petInfoStateDataSource: PetInfoStateDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<Any, List<ChatItems>>(ioDispatcher) {

  override fun execute(parameters: Any): Flow<Result<List<ChatItems>>> {
    return flow {
      conversationRepository.getAllConversations().map {
        it.map { conversations ->
          val petInfo =
            petInfoStateDataSource.getCollectionInfo(conversations.recipientId)
          val name = if (conversations.fromSender) conversations.petName else null
          petInfo.map { result ->
            ChatItems(
              name = name ?: (result.data?.petName() ?: "Unavailable"),
              profile = result.data?.petPrimaryProfile() ?: "",
              recentMessage = conversations.content,
              petId = conversations.petId,
              recipientId = conversations.recipientId
            )
          }.first()
        }
      }.collect { chatList ->
        emit(Result.Success(chatList))
      }
    }
  }
}
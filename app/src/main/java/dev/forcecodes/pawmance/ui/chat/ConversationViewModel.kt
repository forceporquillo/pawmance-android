package dev.forcecodes.pawmance.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.pawmance.data.auth.PetInfoStateDataSource
import dev.forcecodes.pawmance.data.conversation.ConversationRepository
import dev.forcecodes.pawmance.model.ConversationUiModel
import dev.forcecodes.pawmance.model.MessageData
import dev.forcecodes.pawmance.model.PetTokenData
import dev.forcecodes.pawmance.ui.signin.SignInViewModelDelegate
import dev.forcecodes.pawmance.utils.Result
import dev.forcecodes.pawmance.utils.data
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
  private val petInfoStateDataSource: PetInfoStateDataSource,
  private val conversationRepository: ConversationRepository,
  signInViewModelDelegate: SignInViewModelDelegate
) : ViewModel() {

  private var senderName: String? = null
  private var senderId: String? = null
  private var recipientId: String? = null
  private var recipientToken: String? = null
  private var messageContent: String? = null

  private val _conversations = MutableStateFlow<List<ConversationUiModel>>(emptyList())
  val conversations = _conversations.asStateFlow()

  private var profileImage: String? = null

  init {
    viewModelScope.launch {
      signInViewModelDelegate.userId.map { uid ->
        petInfoStateDataSource.getCollectionInfo(uid ?: return@map).collect { result ->
          if (result is Result.Success) {
            senderName = result.data?.petName()
            senderId = uid
          }
        }
      }.collect()
    }
  }

  fun getConversations(petTokenData: PetTokenData) {
    this.recipientId = petTokenData.petId

    getTokenIfNullOrEmpty(petTokenData)

    viewModelScope.launch {
      conversationRepository.getConversations(petTokenData.petId).map {
        it.map { conversations ->
          if (profileImage == null && conversations.fromSender) {
            petInfoStateDataSource.getCollectionInfo(petTokenData.petId).map { result ->
              profileImage = result.data?.petPrimaryProfile()
            }.first()
          }
          ConversationUiModel(
            content = conversations.content,
            petName = conversations.petName,
            petId = conversations.petId,
            fromSender = conversations.fromSender,
            recipientId = conversations.recipientId,
            id = conversations.id,
            petPetProfile = profileImage
          )
        }
      }.collect {
        _conversations.value = it
      }
    }
  }

  private fun getTokenIfNullOrEmpty(petTokenData: PetTokenData) {
    if (petTokenData.tokenId.isNullOrEmpty()) {
      viewModelScope.launch {
        petInfoStateDataSource.getCollectionInfo(petTokenData.petId).collect {
          if (it is Result.Success) {
            recipientToken = it.data?.getPetTokenId()
          }
        }
      }
    } else {
      this.recipientToken = petTokenData.tokenId
    }
  }

  fun sendMessage(content: String) {
    messageContent = content
  }

  fun onMessageSend() {
    viewModelScope.launch {
      if (isMessagePreconditionsEmpty()) {
        return@launch
      }
      val messageData = MessageData(messageContent!!, senderName!!, senderId!!, recipientId!!)
      conversationRepository.sendMessage(messageData, recipientToken!!, recipientId)
    }
  }

  private fun isMessagePreconditionsEmpty(): Boolean {
    return senderName == null || senderId == null || messageContent == null
      || recipientToken == null || recipientId == null
  }
}
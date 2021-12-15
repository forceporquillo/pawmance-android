package dev.apes.pawmance.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.data.auth.PetInfoStateDataSource
import dev.apes.pawmance.data.conversation.ConversationRepository
import dev.apes.pawmance.data.conversation.CreateChannelSource
import dev.apes.pawmance.model.ConversationUiModel
import dev.apes.pawmance.model.MessageData
import dev.apes.pawmance.model.PetTokenData
import dev.apes.pawmance.ui.signin.SignInViewModelDelegate
import dev.apes.pawmance.utils.Result
import dev.apes.pawmance.utils.TimeUtils
import dev.apes.pawmance.utils.data
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.Forest
import javax.inject.Inject
import kotlin.math.sign

@HiltViewModel
class ConversationViewModel @Inject constructor(
  private val petInfoStateDataSource: PetInfoStateDataSource,
  private val conversationRepository: ConversationRepository,
  private val channelSource: CreateChannelSource,
  private val signInViewModelDelegate: SignInViewModelDelegate
) : ViewModel() {

  private var senderName: String? = null
  private var senderId: String? = null
  private var recipientId: String? = null
  private var recipientToken: String? = null
  private var messageContent: String? = null

  private val _conversations = MutableStateFlow<List<ConversationUiModel>>(emptyList())
  val conversations = _conversations.asStateFlow()

  private val _profileImage = MutableStateFlow<String?>(null)
  val profileImage = _profileImage.asStateFlow()

  private val _recipientName = MutableStateFlow<String?>(null)
  val recipientName = _recipientName.asStateFlow()

  private val _channelId = MutableStateFlow<String?>(null)
  val channelId = _channelId.asStateFlow()

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

  fun getChannel(theirPetId: String) = flow {
    emitAll(channelSource.createChannel(theirPetId, signInViewModelDelegate.userIdValue!!))
  }

  fun getConversations(petTokenData: PetTokenData) {
    this.recipientId = petTokenData.petId

    getTokenIfNullOrEmpty(petTokenData)

    viewModelScope.launch {
      conversationRepository.getConversations(petTokenData.petId).map {
        it.map { conversations ->
          if (_profileImage.value == null && conversations.fromSender) {
            petInfoStateDataSource.getCollectionInfo(petTokenData.petId).map { result ->
              _profileImage.value = result.data?.petPrimaryProfile()
              _recipientName.value = result.data?.petName()
            }.first()
          }
          if (conversations.fromSender) {
            ConversationUiModel.SenderUiModel(
                content = conversations.content,
                petName = conversations.petName,
                petId = conversations.petId,
                recipientId = conversations.recipientId,
                id = conversations.id,
                petPetProfile = _profileImage.value,
                timestamp = TimeUtils.convertDate(epochMilli = conversations.timestamp).orEmpty()
            )
          } else {
            ConversationUiModel.ReceivedUiModel(
              content = conversations.content,
              petName = conversations.petName,
              petId = conversations.petId,
              recipientId = conversations.recipientId,
              id = conversations.id,
              petPetProfile = _profileImage.value,
              timestamp = TimeUtils.convertDate(epochMilli = conversations.timestamp).orEmpty()
            )
          }
        }
      }.collect { value: List<ConversationUiModel> ->
        // val test = value.groupBy { uiModel ->
        //   uiModel.timestamp
        // }.mapValues { entries ->
        //   entries.value.map {
        //     it
        //   }
        // }.map { (dateHeaderModel, conversationUiModel) ->
        //   val mutableList = mutableListOf<ConversationUiModel>()
        //   mutableList.add(ConversationUiModel.DateHeaderUiModel(dateHeaderModel))
        //   mutableList.addAll(conversationUiModel)
        //   mutableList
        // }.forEach {
        //
        // }
         _conversations.value = value
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
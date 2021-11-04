package dev.forcecodes.pawmance.ui

import androidx.lifecycle.ViewModel
import dev.forcecodes.pawmance.utils.tryOffer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BaseViewModel<E : UiActionEvent> : ViewModel() {

  protected val mIsLoading = MutableStateFlow(false)

  protected val mUiEvents = Channel<E>(capacity = Channel.CONFLATED)

  @Suppress("UNCHECKED_CAST")
  fun sendUiEvent(event: UiActionEvent) {
    mUiEvents.tryOffer(event as E)
  }
}
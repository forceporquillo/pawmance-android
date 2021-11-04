package dev.forcecodes.pawmance.ui.registration

import dev.forcecodes.pawmance.ui.UiActionEvent

sealed class AddPetDetailsUiEvent : UiActionEvent {
  object ProceedActionEvent : AddPetDetailsUiEvent()
}
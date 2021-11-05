package dev.apes.pawmance.ui.registration

import dev.apes.pawmance.ui.UiActionEvent

sealed class AddPetDetailsUiEvent : UiActionEvent {
  object ProceedActionEvent : AddPetDetailsUiEvent()
}
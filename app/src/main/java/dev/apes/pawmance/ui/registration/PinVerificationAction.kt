package dev.apes.pawmance.ui.registration

import dev.apes.pawmance.ui.UiActionEvent

sealed class PinVerificationAction : UiActionEvent {
  object VerifyPhoneNumber : PinVerificationAction()
}
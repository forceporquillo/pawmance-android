package dev.forcecodes.pawmance.ui.registration

import dev.forcecodes.pawmance.ui.UiActionEvent

sealed class PinVerificationAction : UiActionEvent {
  object VerifyPhoneNumber : PinVerificationAction()
}
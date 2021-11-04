package dev.forcecodes.pawmance.ui.registration

import android.telephony.PhoneNumberUtils
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.pawmance.data.auth.AuthStateDataSource
import dev.forcecodes.pawmance.ui.BaseViewModel
import dev.forcecodes.pawmance.utils.Result
import dev.forcecodes.pawmance.utils.isValidPhoneNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PhoneRegistrationViewModel @Inject constructor(
  private val authStateDataSource: AuthStateDataSource
) : BaseViewModel<PinVerificationAction>() {

  val verifyPinUiActionEvent = mUiEvents.receiveAsFlow()

  private val _isInvalidFormat = MutableStateFlow("")
  val isInvalidFormat: StateFlow<String> = _isInvalidFormat.asStateFlow()

  private val _isEnabled = MutableStateFlow(false)
  val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

  private val _phoneNumber = MutableStateFlow("")
  val phoneNumber = _phoneNumber.asStateFlow()

  var verificationId: String? = null
  var resendToken: PhoneAuthProvider.ForceResendingToken? = null

  private val _phoneVerificationUiState = MutableStateFlow(PhoneVerificationUiState())
  val phoneVerificationUiState = _phoneVerificationUiState.asStateFlow()

  fun isValid(phone: String?) {
    _isEnabled.value = phone?.length == 10 && phone.startsWith("9")
    phone?.isValidPhoneNumber(_isInvalidFormat)
  }

  fun setPhoneNumber(number: String?) {
    _phoneNumber.value = PhoneNumberUtils
      .formatNumberToE164(number ?: return, "PH")
  }

  fun setVerificationCode(verificationCode: String) {
    authenticate(PhoneAuthProvider.getCredential(verificationId!!, verificationCode))
  }

  fun phoneCredentials(credentials: PhoneAuthCredential) {
    authenticate(credentials)
  }

  private fun authenticate(credential: PhoneAuthCredential) {
    viewModelScope.launch {
      authStateDataSource.loginWithPhone(credential).collect { result ->
        _phoneVerificationUiState.value = when (result) {
          is Result.Loading -> {
            PhoneVerificationUiState(true)
          }
          is Result.Success -> {
            PhoneVerificationUiState(isLoading = false, true)
          }
          is Result.Error -> {
            PhoneVerificationUiState(isLoading = false, exception = result.exception)
          }
        }
      }
    }
  }

  fun onCodeSent(
    verificationId: String,
    resendToken: PhoneAuthProvider.ForceResendingToken
  ) {
    Timber.e("onCodeSent")
    this.verificationId = verificationId
    this.resendToken = resendToken

    sendUiEvent(PinVerificationAction.VerifyPhoneNumber)
  }

  data class PhoneVerificationUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val exception: Exception? = null
  )
}


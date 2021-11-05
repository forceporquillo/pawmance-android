package dev.apes.pawmance.ui.registration

import android.telephony.PhoneNumberUtils
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.data.auth.AuthStateDataSource
import dev.apes.pawmance.data.auth.LoginAuthResult
import dev.apes.pawmance.data.auth.PetInfoStateDataSource
import dev.apes.pawmance.ui.BaseViewModel
import dev.apes.pawmance.utils.Result
import dev.apes.pawmance.utils.Result.Error
import dev.apes.pawmance.utils.Result.Loading
import dev.apes.pawmance.utils.Result.Success
import dev.apes.pawmance.utils.isValidPhoneNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PhoneRegistrationViewModel @Inject constructor(
  private val authStateDataSource: AuthStateDataSource,
  private val petInfoStateDataSource: PetInfoStateDataSource
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
      authStateDataSource.loginWithPhone(credential).flatMapConcat { result ->
        isUserExist(result)
      }.collect {
        Timber.e("PhoneVerificationUiState $it")
        _phoneVerificationUiState.value = it
      }
    }
  }

  private suspend fun isUserExist(loginResult: Result<LoginAuthResult>): Flow<PhoneVerificationUiState> {
    return flow {
      when (loginResult) {
        is Loading -> {
          emit(PhoneVerificationUiState(true))
        }
        is Success -> {
          val useId = loginResult.data.currentUser?.uid
          Timber.e(useId)
          if (useId == null) {
            emit(PhoneVerificationUiState(isLoading = false, loginResult.data.success, true))
          } else {
            petInfoStateDataSource.getCollectionInfo(useId).map { result ->
              if (result is Success) {
                result.data?.petName() == null
              } else {
                true
              }
            }.debounce(2000L)
              .collect {
              emit(PhoneVerificationUiState(isLoading = false, loginResult.data.success, it))
            }
          }
        }
        is Error -> {
          emit(PhoneVerificationUiState(isLoading = false, exception = loginResult.exception))
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
    val completeProfile: Boolean? = null,
    val exception: Exception? = null
  )
}


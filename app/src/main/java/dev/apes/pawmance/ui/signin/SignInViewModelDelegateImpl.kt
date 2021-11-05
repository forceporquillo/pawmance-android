package dev.apes.pawmance.ui.signin

import android.net.Uri
import dev.apes.pawmance.data.auth.AuthStateDataSource
import dev.apes.pawmance.data.auth.AuthenticatedUserInfoBasic
import dev.apes.pawmance.di.ApplicationScope
import dev.apes.pawmance.utils.Result
import dev.apes.pawmance.utils.WhileViewSubscribed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SignInViewModelDelegateImpl @Inject constructor(
  authStateDataSource: AuthStateDataSource,
  @ApplicationScope private val applicationScope: CoroutineScope
) : SignInViewModelDelegate {

  private val _signInNavigationActions = Channel<AdminAuthState>(Channel.CONFLATED)
  override val signInNavigationActionsImpl = _signInNavigationActions.receiveAsFlow()

  private val currentAdminUser: Flow<Result<AuthenticatedUserInfoBasic>> =
    authStateDataSource.getAuthenticatedBasicInfo().map { result ->
      if (result is Result.Error) {
        Timber.e(result.exception.message.toString())
      }
      result
    }

  override val userInfo: StateFlow<AuthenticatedUserInfoBasic?> = currentAdminUser.map {
    (it as? Result.Success)?.data
  }.stateIn(applicationScope, WhileViewSubscribed, null)

  override val currentUserImageUri: StateFlow<Uri?> = userInfo.map {
    it?.getPhotoUrl()
  }.stateIn(applicationScope, WhileViewSubscribed, null)

  override val isUserSignedIn: StateFlow<Boolean> = userInfo.map {
    it?.isSignedIn() ?: false
  }.stateIn(applicationScope, WhileViewSubscribed, false)

  override val userId: Flow<String?>
    get() = userInfo.mapLatest { it?.getUid() }
      .stateIn(applicationScope, WhileViewSubscribed, null)

  override val userIdValue: String?
    get() = userInfo.value?.getUid()

  override val isUserSignedInValue: Boolean
    get() = isUserSignedIn.value

  init {
    applicationScope.launch {
      userInfo.debounce(500L).collectLatest {
        if (it?.isSignedIn() == true) {
          _signInNavigationActions.send(AdminAuthState.SignedIn)
        } else {
          _signInNavigationActions.send(AdminAuthState.SignedOut)
        }

        Timber.e(it?.getUid().toString())
      }
    }
  }

  sealed class AdminAuthState {
    object SignedOut : AdminAuthState()
    object SignedIn : AdminAuthState()
  }
}
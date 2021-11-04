package dev.forcecodes.pawmance.ui.registration

import dev.forcecodes.pawmance.ui.BaseViewModel
import dev.forcecodes.pawmance.ui.signin.SignInViewModelDelegate
import dev.forcecodes.pawmance.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

abstract class CompletePetDetailsViewModel : BaseViewModel<AddPetDetailsUiEvent>() {

  @Inject
  lateinit var signInViewModelDelegate: SignInViewModelDelegate

  private val mAddPetUiState = MutableStateFlow(PetUiState())
  val addPetUiState = mAddPetUiState.asStateFlow()

  private val mEnableSubmitButton = MutableStateFlow(false)
  val enableSubmit = mEnableSubmitButton.asStateFlow()

  data class PetUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = ""
  )

  protected fun <T> filterResult(result: Result<T>) {
    mAddPetUiState.value = when (result) {
      is Result.Loading -> PetUiState(true)
      is Result.Success -> {
        sendUiEvent(AddPetDetailsUiEvent.ProceedActionEvent)
        PetUiState(false)
      }
      is Result.Error -> PetUiState(errorMessage = result.exception.message)
    }
  }

  protected fun enableSubmitButton(enable: Boolean) {
    mEnableSubmitButton.value = enable
  }
}

package dev.apes.pawmance.ui.registration

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentVerifyPhoneNumberBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.ui.MainActivity
import dev.apes.pawmance.ui.registration.PhoneRegistrationViewModel.PhoneVerificationUiState
import dev.apes.pawmance.utils.createIntent
import dev.apes.pawmance.utils.fromHtmlWithParams
import dev.apes.pawmance.utils.repeatOnLifecycle
import dev.apes.pawmance.utils.showToast
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class VerifyPhoneNumberFragment : Fragment(R.layout.fragment_verify_phone_number) {

  private val viewModel by activityViewModels<PhoneRegistrationViewModel>()
  private val binding by viewBinding(FragmentVerifyPhoneNumberBinding::bind)

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    repeatOnLifecycle {
      viewModel.phoneNumber.collect { phoneNumber ->
        binding.changeYourPhoneNumber.text =
          context?.fromHtmlWithParams(R.string.sms_verification_message, phoneNumber)
        resendVerificationCode(phoneNumber)
      }
    }

    binding.verifyBtn.setOnClickListener { viewModel.setVerificationCode(getVerificationCode()) }
    repeatOnLifecycle { viewModel.phoneVerificationUiState.collect(::setUiState) }
  }

  private fun setUiState(uiState: PhoneVerificationUiState) = with(binding) {
    progressBar.isVisible = uiState.isLoading
    otpErrorMessage.text = uiState.exception?.message

    Timber.e(uiState.toString())
    if (!uiState.isSuccess) {
      return@with
    }

    uiState.completeProfile?.let { shouldComplete ->
      if (shouldComplete) {
        createIntent(CompleteRegistrationActivity::class)
      } else {
        createIntent(MainActivity::class)
        requireActivity().finish()
      }
    }

  }

  private fun resendVerificationCode(phoneNumber: String) {
    binding.recentCode.setOnClickListener {
      showToast("Resending code.")
      (activity as? PhoneRegistrationActivity)?.setPhoneOptions(phoneNumber)
    }
  }

  private fun getVerificationCode(): String {
    return binding.pinViews()
      .joinToString("") { it.text.toString() }
  }

  private fun FragmentVerifyPhoneNumberBinding.pinViews() =
    arrayOf(pin1, pin2, pin3, pin4, pin5, pin6)
}
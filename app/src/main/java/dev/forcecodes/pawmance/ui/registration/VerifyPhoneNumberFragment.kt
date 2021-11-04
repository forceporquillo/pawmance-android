package dev.forcecodes.pawmance.ui.registration

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentVerifyPhoneNumberBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.pawmance.binding.viewBinding
import dev.forcecodes.pawmance.ui.registration.PhoneRegistrationViewModel.PhoneVerificationUiState
import dev.forcecodes.pawmance.utils.createIntent
import dev.forcecodes.pawmance.utils.fromHtmlWithParams
import dev.forcecodes.pawmance.utils.repeatOnLifecycle
import dev.forcecodes.pawmance.utils.showToast
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class VerifyPhoneNumberFragment : Fragment(R.layout.fragment_verify_phone_number) {

  private val viewModel by activityViewModels<PhoneRegistrationViewModel>()
  private val binding by viewBinding(FragmentVerifyPhoneNumberBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

    if (uiState.isSuccess) {
      createIntent(CompleteRegistrationActivity::class)
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
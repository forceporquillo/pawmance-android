package dev.apes.pawmance.ui.registration

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentRegistrationBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhoneRegistrationFragment : Fragment(R.layout.fragment_registration) {

  private val viewModel by activityViewModels<PhoneRegistrationViewModel>()
  private val binding by viewBinding(FragmentRegistrationBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = viewModel

    binding.phoneNumberEditText.addTextChangedListener {
      viewModel.isValid(it.toString())
    }

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.isInvalidFormat.collect {
          binding.phoneNumberTextLayout.error = it
        }
      }
    }

    binding.continueButton.setOnClickListener {
      val phoneNumber = getPhoneNumber()
      viewModel.setPhoneNumber(phoneNumber)
      binding.progressBar.isVisible = true
    }

    repeatOnLifecycle {
      viewModel.verifyPinUiActionEvent.collect {
        binding.progressBar.isVisible = false
        findNavController()
          .navigate(R.id.action_phoneRegistrationFragment_to_verifyPhoneNumberFragment)
      }
    }
  }

  private fun getPhoneNumber() = binding.phoneNumberEditText.text.toString()
}
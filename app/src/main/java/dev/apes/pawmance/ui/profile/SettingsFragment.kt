package dev.apes.pawmance.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.ui.LauncherActivity
import dev.apes.pawmance.ui.signin.SignInViewModelDelegateImpl
import dev.apes.pawmance.utils.getMaxPreferredDistance
import dev.apes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

  @Inject
  lateinit var firebaseAuth: FirebaseAuth

  private val viewModel by viewModels<SettingsViewModel>()
  private val binding by viewBinding(FragmentSettingsBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }

    val maxPrefDistance = requireContext()
      .getMaxPreferredDistance()
      .toFloat()

    binding.slider.value = maxPrefDistance
    setDistance(maxPrefDistance)

    repeatOnLifecycle {
      viewModel.userInfo.collect {
        binding.registeredId.text = it?.getUid()
        binding.contactNumber.text = it?.getPhoneNumber()

        val email = if (it?.getEmail().isNullOrEmpty())
          getString(R.string.not_available) else it?.getEmail()

        binding.email.text = email
      }
    }

    with(binding.slider) {
      addOnChangeListener { _, value, _ ->
        setDistance(value)
        viewModel.maxPrefDistance = value
      }
      setLabelFormatter { value ->
        value.toInt().toString()
      }
    }

    binding.logoutButton.setOnClickListener {
      firebaseAuth.signOut()

    }

    binding.deleteButton.setOnClickListener {
      firebaseAuth.currentUser
        ?.delete()
        ?.addOnCompleteListener { task ->
          if (task.isSuccessful) {
            requestReAuthentication()
          }
        }
    }

    repeatOnLifecycle {
      viewModel.signInNavigationActionsImpl.collect { authState ->
        if (authState is SignInViewModelDelegateImpl.AdminAuthState.SignedOut) {
          requestReAuthentication()
        }
      }
    }
  }

  private fun requestReAuthentication() {
    repeatOnLifecycle {
      delay(500L)
      val activity = requireActivity()
      val intent = Intent(activity, LauncherActivity::class.java)
      intent.putExtra("logout", true)
      startActivity(intent)
      activity.finishAffinity()
    }
  }

  private fun setDistance(value: Float) {
    val distance = value.toInt()
    val kilometer = if (distance > 1) "kms" else "km"
    val distanceInKms = "< ${value.toInt()} $kilometer"
    binding.desiredDistance.text = distanceInKms
  }
}


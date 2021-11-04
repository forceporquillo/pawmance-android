package dev.forcecodes.pawmance.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.ActivityLauncherBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.pawmance.binding.viewBinding
import dev.forcecodes.pawmance.ui.registration.PhoneRegistrationActivity
import dev.forcecodes.pawmance.ui.signin.SignInViewModelDelegateImpl.AdminAuthState
import dev.forcecodes.pawmance.utils.createIntent
import dev.forcecodes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import timber.log.Timber

private val PERMISSIONS = arrayOf(
  Manifest.permission.CAMERA,
  Manifest.permission.READ_EXTERNAL_STORAGE,
  Manifest.permission.ACCESS_FINE_LOCATION,
  Manifest.permission.ACCESS_COARSE_LOCATION
)

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity(R.layout.activity_launcher) {

  private val viewModel by viewModels<LauncherViewModel>()

  private val binding by viewBinding(ActivityLauncherBinding::inflate)

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { observeLaunchScreen() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val isGranted = isPermissionGranted()

    showLoading(isGranted)

    if (!isGranted) {
      showPermissionDialog()
    } else {
      observeLaunchScreen()
    }
  }

  private fun isPermissionGranted(): Boolean {
    var isGranted = false
    for (permission in PERMISSIONS) {
      if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        isGranted = false
        break
      } else {
        isGranted = true
      }
    }
    return isGranted
  }

  private fun observeLaunchScreen() {
    Timber.e("observeLaunchScreen")
    showLoading(true)

    intent.extras?.getBoolean("logout")?.let {
      initViews()
      showLoading(true)
    }

    repeatOnLifecycle {
      viewModel.signInNavigationActionsImpl.collect { value: AdminAuthState ->
        if (value is AdminAuthState.SignedIn) {
          createIntent(MainActivity::class)
          return@collect
        }

        initViews()
      }
    }
  }

  private fun initViews() {
    setContentView(binding.root)
    binding.loginButton.setOnClickListener {
      createIntent(PhoneRegistrationActivity::class)
    }
  }

  private fun showLoading(show: Boolean) = with(binding) {
    root.post {
      progressBar.isGone = show // invert
      troubleshoot.isVisible = show
      loginButton.isVisible = show
      agreement.isVisible = show
    }
  }

  private fun showPermissionDialog() {
    MaterialAlertDialogBuilder(this, R.style.PermissionDialog)
      .setTitle(getString(R.string.request_permission))
      .setMessage(getString(R.string.permission_message))
      .setPositiveButton(R.string.allow_permission) { _, _ ->
        requestPermissionLauncher.launch(PERMISSIONS)
      }
      .setCancelable(false)
      .show()
  }
}




package dev.forcecodes.pawmance.ui.registration

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.devforcecodes.pawmance.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class PhoneRegistrationActivity : BaseRegistrationActivity(R.navigation.registration_nav_graph) {

  @Inject
  lateinit var firebaseAuth: FirebaseAuth

  private val viewModel by viewModels<PhoneRegistrationViewModel>()

  private val verificationCallback = object : OnVerificationStateChangedCallbacks() {
    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
      viewModel.phoneCredentials(credential)
    }

    override fun onVerificationFailed(exception: FirebaseException) {
      Toast.makeText(
        applicationContext,
        "onVerificationFailed: $exception",
        Toast.LENGTH_SHORT
      ).show()
    }

    override fun onCodeSent(
      verificationId: String,
      token: PhoneAuthProvider.ForceResendingToken
    ) {
      super.onCodeSent(verificationId, token)
      viewModel.onCodeSent(verificationId, token)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    repeatOnLifecycle { viewModel.phoneNumber.collect(::setPhoneOptions) }
  }

  fun setPhoneOptions(phoneNumber: String) {
    if (phoneNumber.isEmpty()) {
      return
    }

    val options = PhoneAuthOptions.newBuilder(firebaseAuth)
      .setPhoneNumber(phoneNumber)
      .setTimeout(120L, TimeUnit.SECONDS)
      .setCallbacks(verificationCallback)
      .setActivity(this)
      .build()

    PhoneAuthProvider.verifyPhoneNumber(options)
  }
}
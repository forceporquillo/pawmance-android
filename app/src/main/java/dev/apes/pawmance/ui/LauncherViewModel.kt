package dev.apes.pawmance.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.ui.signin.SignInViewModelDelegate
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
  signInViewModelDelegate: SignInViewModelDelegate
) : ViewModel(), SignInViewModelDelegate by signInViewModelDelegate


package dev.forcecodes.pawmance.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.pawmance.PawManceApplication
import dev.forcecodes.pawmance.ui.signin.SignInViewModelDelegate
import dev.forcecodes.pawmance.utils.storeMaxLocationPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  signInViewModelDelegate: SignInViewModelDelegate,
  application: Application
) : AndroidViewModel(application), SignInViewModelDelegate by signInViewModelDelegate {

  private val _maxPreferredDistance = MutableStateFlow(35f)

  var maxPrefDistance: Float = 35.0f
    set(value) {
      _maxPreferredDistance.value = value
      field = value
    }

  init {
    viewModelScope.launch {
      _maxPreferredDistance.asStateFlow()
        .debounce(500)
        .collect { maxPrefDistance ->
          getApplication<PawManceApplication>()
            .storeMaxLocationPrefs(maxPrefDistance)
        }
    }
  }
}
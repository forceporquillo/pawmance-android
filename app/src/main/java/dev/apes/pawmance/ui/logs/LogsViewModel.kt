package dev.apes.pawmance.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.data.logs.Logs
import dev.apes.pawmance.data.logs.LogsDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
  private val logsDao: LogsDao
) : ViewModel() {

  private val _logs = MutableStateFlow<List<Logs>>(emptyList())
  val logs = _logs.asStateFlow()

  init {
    viewModelScope.launch {
      logsDao.getAllLogs().collect {
        _logs.value = it
      }
    }
  }
}
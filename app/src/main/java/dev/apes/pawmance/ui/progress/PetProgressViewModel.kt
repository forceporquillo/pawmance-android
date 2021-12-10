package dev.apes.pawmance.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.data.progress.DateMated
import dev.apes.pawmance.data.progress.PetProgress
import dev.apes.pawmance.data.progress.PetProgressDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetProgressViewModel @Inject constructor(
  private val petProgressDataSource: PetProgressDataSource
) : ViewModel() {

  private var note: String? = null
  private var day: String? = null

  private val _allProgress = MutableStateFlow<List<PetProgress>?>(null)
  val progress = _allProgress.asStateFlow()

  private val _progressByDay = MutableStateFlow<PetProgress?>(null)
  val progressByDay = _progressByDay.asStateFlow()

  private val _dateMated = MutableStateFlow<DateMated?>(null)
  val dateMated = _dateMated.asStateFlow()

  init {
    viewModelScope.launch {
      launch {
        petProgressDataSource.getAllProgress().collect {
          _allProgress.value = it
        }
      }
      launch {
        petProgressDataSource.getMatedDate().collect {
          _dateMated.value = it
        }
      }
    }
  }

  fun addDateMated(dateInString: String, timeInMillis: Long) {
    viewModelScope.launch {
      petProgressDataSource.addMatingDate(DateMated(dateInString, timeInMillis))
    }
  }

  fun getProgressByDay(
    day: String
  ) {
    viewModelScope.launch {
      petProgressDataSource.getProgressNote(day).collect {
        _progressByDay.value = it
      }
    }
  }

  fun addProgressNote(
    note: String,
    day: String?
  ) {
    this.note = note
    this.day = day
  }

  fun onUpdateStatusButton() {
    if (note == null && day == null) {
      return
    }

    viewModelScope.launch {
      val petProgress = PetProgress(note!!, false, day!!)
      petProgressDataSource.addProgressNote(petProgress)
    }
  }
}
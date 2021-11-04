package dev.forcecodes.pawmance.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.pawmance.data.progress.PetProgress
import dev.forcecodes.pawmance.data.progress.PetProgressDataSource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetProgressViewModel @Inject constructor(
  private val petProgressDataSource: PetProgressDataSource
): ViewModel() {

  private var note: String = ""
  private var day: String? = null

  fun addProgressNote(note: String, day: String?) {
    this.note = note
    this.day = day
  }

  fun onUpdateStatusButton() {
    val data = mapOf(day to note)
    viewModelScope.launch {
     // petProgressDataSource.addProgressNote(PetProgress())
    }
  }
}
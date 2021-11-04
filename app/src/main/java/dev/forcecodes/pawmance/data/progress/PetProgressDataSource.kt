package dev.forcecodes.pawmance.data.progress

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dev.forcecodes.pawmance.utils.Result
import dev.forcecodes.pawmance.utils.Result.Loading
import dev.forcecodes.pawmance.utils.Result.Success
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class PetProgress(
  val id: String,
  val note: String,
  val day: String
)

class PetProgressDataSourceImpl @Inject constructor(
  private val firebaseFirestore: FirebaseFirestore
): PetProgressDataSource {

  companion object {
    private const val PROGRESS = "progress"
  }

  override fun addProgressNote(petProgress: PetProgress): Flow<Result<Boolean>> {
    val (id, note, day) = petProgress

    return flow {
      emit(Loading)
      firebaseFirestore
        .collection(PROGRESS)
        .document(id)
        .set(mapOf(day to note), SetOptions.merge())
        .await()
      emit(Success(true))
    }
  }

  override fun getAllProgressNote(id: String): Flow<List<PetProgress>> {
    return callbackFlow {
      val listenerRegistration = firebaseFirestore.collection(PROGRESS)
        .document(id)

        .addSnapshotListener { value, error ->
          if (value?.exists() == true) {

          }
        }

      awaitClose { listenerRegistration.remove() }
    }
  }
}

interface PetProgressDataSource {
  fun addProgressNote(petProgress: PetProgress): Flow<Result<Boolean>>
  fun getAllProgressNote(id: String): Flow<List<PetProgress>>

}
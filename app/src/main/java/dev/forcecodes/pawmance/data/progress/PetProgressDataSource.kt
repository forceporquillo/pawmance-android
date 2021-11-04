package dev.forcecodes.pawmance.data.progress

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dev.forcecodes.pawmance.db.AppDatabase
import dev.forcecodes.pawmance.di.IoDispatcher
import dev.forcecodes.pawmance.utils.Result
import dev.forcecodes.pawmance.utils.Result.Loading
import dev.forcecodes.pawmance.utils.Result.Success
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Entity
data class PetProgress(
  val note: String,

  @PrimaryKey
  val day: String
)

class PetProgressDataSourceImpl @Inject constructor(
  private val progressDao: ProgressDao,
  private val firebaseFirestore: FirebaseFirestore,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
): PetProgressDataSource {

  companion object {
    private const val PROGRESS = "progress"
  }

  override suspend fun addProgressNote(petProgress: PetProgress) {
    withContext(dispatcher) {
      progressDao.insertProgress(petProgress)
    }
  }

  override fun getProgressNote(day: String): Flow<PetProgress?> {
   return progressDao.getAllProgress(day)
  }

  override fun getAllProgress(): Flow<List<PetProgress>> {
    return progressDao.getAllProgress()
  }
}

interface PetProgressDataSource {
  suspend fun addProgressNote(petProgress: PetProgress)
  fun getProgressNote(day: String): Flow<PetProgress?>
  fun getAllProgress(): Flow<List<PetProgress>>

}
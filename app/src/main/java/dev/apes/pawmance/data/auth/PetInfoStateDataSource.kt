package dev.apes.pawmance.data.auth

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import dev.apes.pawmance.utils.Result
import dev.apes.pawmance.utils.tryOffer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface PetInfoStateDataSource {
  fun getCollectionInfo(id: String): Flow<Result<PetInfo?>>
}

@Singleton
class FirestorePetInfoStateDataSource @Inject constructor(
  private val firebaseFirestore: FirebaseFirestore
) : PetInfoStateDataSource {

  override fun getCollectionInfo(id: String): Flow<Result<PetInfo>> {
    return callbackFlow {

      val petCollectionSnapShotListener: (DocumentSnapshot?, FirebaseFirestoreException?) -> Unit =
        { documentSnapshot, exception ->

          if (documentSnapshot?.exists() == false) {
            tryOffer(Result.Error(Exception("Collection data of $id is empty.")))
          } else {
            val petCollection = documentSnapshot?.toObject<PetCollection>()
            tryOffer(Result.Success(PetInfo(petCollection)))
          }
          if (exception != null) {
            tryOffer(Result.Error(exception))
          }
        }

      val listenerRegistration = firebaseFirestore.collection("users")
        .document(id)
        .addSnapshotListener(petCollectionSnapShotListener)

      awaitClose {
        listenerRegistration.remove()
      }
    }
  }
}
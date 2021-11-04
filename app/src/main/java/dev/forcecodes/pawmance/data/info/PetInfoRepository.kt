package dev.forcecodes.pawmance.data.info

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import dev.forcecodes.pawmance.di.ApplicationScope
import dev.forcecodes.pawmance.di.IoDispatcher
import dev.forcecodes.pawmance.model.MyLocation
import dev.forcecodes.pawmance.model.PetName
import dev.forcecodes.pawmance.utils.Result
import dev.forcecodes.pawmance.utils.WhileViewSubscribed
import dev.forcecodes.pawmance.utils.data
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.LinkedList
import java.util.Vector
import javax.inject.Inject

fun FirebaseFirestore.userCollection(userId: String): DocumentReference {
  return collection("users").document(userId)
}

internal typealias FlowResult = Flow<Result<Boolean>>

interface PetInfoRepository {
  fun updatePetBreed(id: String, breed: String): FlowResult
  fun addPetName(petName: PetName): FlowResult
  fun addPetBirthdate(id: String, birthDate: String): FlowResult
  fun addPetGender(id: String, gender: String): FlowResult
  fun addPetLocation(id: String, location: MyLocation): FlowResult
  fun addPetPhotos(vectorByteArray: Vector<ByteArray>, id: String): Flow<Result<List<Boolean>>>
  fun addPetPreferences(preferences: LinkedList<String>, id: String): FlowResult
  fun addPetProfilePhoto(uri: Uri, id: String): Flow<Result<Uri>>
  fun addPartnerId(id: String, matchId: String? = null): FlowResult
}

class PetInfoRepositoryImpl @Inject constructor(
  private val firestore: FirebaseFirestore,
  private val cloudStorage: FirebaseStorage,
  @ApplicationScope private val externalScope: CoroutineScope,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PetInfoRepository {

  override fun addPetName(petName: PetName): Flow<Result<Boolean>> {
    val (id, name, breed) = petName
    return addToUserCollection(id, mapOf("name" to name, "breed" to breed))
  }

  override fun addPetBirthdate(id: String, birthDate: String): Flow<Result<Boolean>> {
    return addToUserCollection(id, mapOf("birthdate" to birthDate))
  }

  override fun addPetGender(id: String, gender: String): Flow<Result<Boolean>> {
    return addToUserCollection(id, mapOf("gender" to gender))
  }

  override fun addPetLocation(id: String, location: MyLocation): Flow<Result<Boolean>> {
    return addToUserCollection(id, mapOf("location" to location))
  }

  override fun updatePetBreed(id: String, breed: String): Flow<Result<Boolean>> {
    return addToUserCollection(id, mapOf("breed" to breed))
  }

  override fun addPartnerId(id: String, matchId: String?): Flow<Result<Boolean>> {
    return addToUserCollection(id, mapOf("partnerId" to matchId))
  }

  override fun addPetPreferences(
    preferences: LinkedList<String>,
    id: String
  ): Flow<Result<Boolean>> {
    return addToUserCollection(id, mapOf("preferences" to preferences))
  }

  override fun addPetPhotos(
    vectorByteArray: Vector<ByteArray>,
    id: String
  ): Flow<Result<List<Boolean>>> = flow {
    var resultList: Result<MutableList<Boolean>> = Result.Loading

    emit(resultList)

    val list = mutableListOf<Boolean>()

    for (byte in vectorByteArray) {
      list.add(false)
    }

    resultList = Result.Success(list)

    val cloudStorageRefs = cloudStorage.getReference("profile")
      .child(id)

    vectorByteArray.forEachIndexed { index, bytes ->
      val photoRefs = cloudStorageRefs
        .child("photo$index.jpg")
        .putBytes(bytes)

      photoRefs.continueWithTask { task ->
        if (!task.isSuccessful) {
          resultList = Result.Error(
            Exception(
              "Error uploading photo at index: " +
                "$index caused by ${task.exception}"
            )
          )
        } else {
          resultList.data?.set(index, true)
        }
        cloudStorageRefs
          .child("photo0.jpg")
          .downloadUrl
      }.addOnSuccessListener { uri ->
        // append photo to main auth.
        updateProfileToAuth(uri, id)
      }.await()
    }
    emit(resultList)
  }
    .flowOn(ioDispatcher)

  override fun addPetProfilePhoto(uri: Uri, id: String): Flow<Result<Uri>> {
    return flow {
      var result: Result<Uri> = Result.Loading
      emit(result)

      val storageRefs = cloudStorage.getReference("profile")
        .child(id)
        .child("profile_photo.jpg")

      val uriTask = storageRefs.putFile(uri)

      uriTask.continueWithTask { task ->
        if (!task.isSuccessful) {
          task.exception?.let {
            result = Result.Error(it)
          }
        }
        storageRefs.downloadUrl
      }.addOnCompleteListener {
        result = if (it.isSuccessful) {
          Timber.e("DownloadUri ${it.result}")
          updateProfileToAuth(it.result, id)
          Result.Success(it.result)
        } else {
          Result.Error(Exception(it.exception))
        }
      }.await()
      emit(result)
    }
  }

  private fun <T : Any> addToUserCollection(id: String, data: T): Flow<Result<Boolean>> {
    return flow {
      var result: Result<Boolean> = Result.Loading
      emit(result)
      firestore.userCollection(id)
        .set(data, SetOptions.merge())
        .addOnCompleteListener {
          result = if (it.isSuccessful) {
            Result.Success(true)
          } else {
            Result.Error(it.exception ?: Exception("Unknown error."))
          }
        }.await()
      emit(result)
    }
      .shareIn(externalScope, WhileViewSubscribed, 1)
  }

  private fun updateProfileToAuth(downloadUri: Uri, id: String) {
    externalScope.launch(ioDispatcher) {
      addToUserCollection(id, mapOf("profile" to "$downloadUri")).collect { result ->
        Timber.e("Result $result")
        if (result is Result.Success) {
          val updateRequest = userProfileChangeRequest {
            photoUri = downloadUri
          }

          FirebaseAuth.getInstance()
            .currentUser
            ?.updateProfile(updateRequest)
        }
      }
    }
  }
}
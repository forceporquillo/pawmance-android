package dev.apes.pawmance.data.auth

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.UserInfo
import dev.apes.pawmance.data.fcm.FcmTokenUpdater
import dev.apes.pawmance.di.ApplicationScope
import dev.apes.pawmance.model.MyLocation
import dev.apes.pawmance.utils.Result
import dev.apes.pawmance.utils.tryOffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

interface AuthStateDataSource {
  suspend fun loginWithPhone(credential: PhoneAuthCredential): Flow<Result<LoginAuthResult>>
  fun getAuthenticatedBasicInfo(): Flow<Result<AuthenticatedUserInfoBasic>>
}

@Singleton
class FirebaseAuthStateDataSource @Inject constructor(
  private val firebaseAuth: FirebaseAuth,
  private val tokenUpdater: FcmTokenUpdater,
  @ApplicationScope private val externalScope: CoroutineScope
) : AuthStateDataSource {

  private val basicAuthInfo: SharedFlow<Result<AuthenticatedUserInfoBasic>> =
    callbackFlow {
      val authStateListener: ((FirebaseAuth) -> Unit) = { auth ->
        tryOffer(auth)
      }
      firebaseAuth.apply {
        addAuthStateListener(authStateListener)
        awaitClose { removeAuthStateListener(authStateListener) }
      }
    }
      .map { auth ->
        processAuthState(auth)
      }.shareIn(
        scope = externalScope,
        replay = 1,
        started = SharingStarted.WhileSubscribed()
      )

  private fun processAuthState(auth: FirebaseAuth): Result<AuthenticatedUserInfoBasic> {
    Timber.d("Received a FirebaseAuth update.")

    auth.currentUser?.let { currentUser ->
      // Save the FCM ID token in firestore
      tokenUpdater.updateTokenForUser(currentUser.uid)
    }

    return Result.Success(FirebaseUserInfo(auth.currentUser))
  }

  override suspend fun loginWithPhone(credential: PhoneAuthCredential): Flow<Result<LoginAuthResult>> {
    return flow {
      var isSuccess: Result<LoginAuthResult> = Result.Loading
      emit(isSuccess)
      try {
        firebaseAuth.signInWithCredential(credential)
          .addOnCompleteListener { task ->
            isSuccess = if (task.isSuccessful) {
              Result.Success(LoginAuthResult(true, task.result.user))
            } else {
              Result.Error(Exception(task.exception))
            }
          }.await()
        emit(isSuccess)
      } catch (e: FirebaseAuthException) {
        emit(Result.Error(e))
      }
    }
  }

  override fun getAuthenticatedBasicInfo() = basicAuthInfo
}

data class LoginAuthResult(
  val success: Boolean = false,
  val currentUser: FirebaseUser? = null
)

// region Firebase
suspend fun <T> Task<T>.suspendAndWait(): T =
  suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
      continuation.resume(result, { })
    }
    addOnFailureListener { exception ->
      continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
      continuation.resumeWithException(Exception("Firebase Task was cancelled"))
    }
  }
// endregion

class PetCollection @JvmOverloads constructor(
  val birthdate: String? = null,
  val breed: String? = null,
  val gender: String? = null,
  val location: MyLocation? = null,
  val name: String? = null,
  val preferences: List<String>? = emptyList(),
  val profile: String? = null,
  val partnerId: String? = null,
  val tokenId: String? = null
)

data class PetMetadata(
  val petId: String,
  val collection: PetCollection
)

class PetInfo(
  private val petCollection: PetCollection?
) : CollectionInfo {

  override fun petBirthdate() = petCollection?.birthdate

  override fun petBreed() = petCollection?.breed

  override fun location() = petCollection?.location

  override fun petGender() = petCollection?.gender

  override fun petName() = petCollection?.name

  override fun petPreferences() = petCollection?.preferences

  override fun petPrimaryProfile() = petCollection?.profile

  override fun toString(): String {
    return "PetInfo(petCollection=$petCollection)"
  }

  override fun getPartnerId() = petCollection?.partnerId

  override fun getPetTokenId() = petCollection?.tokenId
}

interface CollectionInfo {

  fun petBirthdate(): String?

  fun petBreed(): String?

  fun location(): MyLocation?

  fun petGender(): String?

  fun petName(): String?

  fun petPreferences(): List<String>?

  fun petPrimaryProfile(): String?

  fun getPartnerId(): String?

  fun getPetTokenId(): String?
}

interface AuthenticatedUserInfoBasic {

  fun isSignedIn(): Boolean

  fun getEmail(): String?

  fun getProviderData(): MutableList<out UserInfo>?

  fun getLastSignInTimestamp(): Long?

  fun getCreationTimestamp(): Long?

  fun getPhoneNumber(): String?

  fun getUid(): String?

  fun isEmailVerified(): Boolean?

  fun getDisplayName(): String?

  fun getPhotoUrl(): Uri?

  fun getProviderId(): String?
}

class FirebaseUserInfo(
  private val firebaseUser: FirebaseUser?
) : AuthenticatedUserInfoBasic {

  override fun isSignedIn(): Boolean = firebaseUser != null

  override fun getEmail(): String? = firebaseUser?.email

  override fun getProviderData(): MutableList<out UserInfo>? = firebaseUser?.providerData

  override fun getPhoneNumber(): String? = firebaseUser?.phoneNumber

  override fun getUid(): String? = firebaseUser?.uid

  override fun isEmailVerified(): Boolean? = firebaseUser?.isEmailVerified

  override fun getDisplayName(): String? = firebaseUser?.displayName

  override fun getPhotoUrl(): Uri? = firebaseUser?.photoUrl

  override fun getProviderId(): String? = firebaseUser?.providerId

  override fun getLastSignInTimestamp(): Long? = firebaseUser?.metadata?.lastSignInTimestamp

  override fun getCreationTimestamp(): Long? = firebaseUser?.metadata?.creationTimestamp
}
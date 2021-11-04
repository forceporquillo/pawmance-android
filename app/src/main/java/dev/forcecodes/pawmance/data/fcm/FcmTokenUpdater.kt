package dev.forcecodes.pawmance.data.fcm

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dev.forcecodes.pawmance.di.ApplicationScope
import dev.forcecodes.pawmance.di.MainDispatcher
import dev.forcecodes.pawmance.utils.petCollection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class FcmTokenUpdater @Inject constructor(
  @ApplicationScope private val externalScope: CoroutineScope,
  @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
  val firestore: FirebaseFirestore
) {

  companion object {
    private const val LAST_VISIT_KEY = "lastVisit"
    private const val TOKEN_ID_KEY = "tokenId"
  }

  fun updateTokenForUser(userId: String) {
    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->

      val tokenInfo = mapOf(
        LAST_VISIT_KEY to FieldValue.serverTimestamp(),
        TOKEN_ID_KEY to token
      )

      externalScope.launch(mainDispatcher) {
        firestore.petCollection()
          .document(userId)
          .set(tokenInfo, SetOptions.merge())
          .addOnCompleteListener {
            if (it.isSuccessful) {
              Timber.d("FCM ID token successfully uploaded for user $userId\"")
            } else {
              Timber.e("FCM ID token: Error uploading for user $userId")
            }
          }
      }
    }
  }
}
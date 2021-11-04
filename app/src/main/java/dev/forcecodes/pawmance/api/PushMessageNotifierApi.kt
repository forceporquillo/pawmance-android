package dev.forcecodes.pawmance.api

import com.devforcecodes.pawmance.BuildConfig
import dev.forcecodes.pawmance.model.PushMessage
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface PushMessageNotifierApi {

  @Headers(
    "Authorization: key=${BuildConfig.FCM_SERVER_API}",
    "Content-Type:application/json"
  )
  @POST("fcm/send")
  suspend fun sendMessage(
    @Body pushMessage: PushMessage
  ): Response<ResponseBody>
}





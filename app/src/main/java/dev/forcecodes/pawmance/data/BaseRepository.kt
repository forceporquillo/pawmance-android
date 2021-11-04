package dev.forcecodes.pawmance.data

import dev.forcecodes.pawmance.api.ApiResponse
import dev.forcecodes.pawmance.api.ApiResponseStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class BaseRepository(private val dispatcher: CoroutineDispatcher) {
  protected suspend fun <T : ApiResponseStatus> getResult(
    call: suspend () -> Response<T>
  ): ApiResponse<T> = withContext(dispatcher) {
    try {
      val response = call()
      if (response.isSuccessful) {
        val body = response.body()
        if (body == null || response.code() == 204) {
          ApiResponse.ApiEmptyResponse()
        } else if (body.status in arrayOf("ZERO_RESULTS", "INVALID_REQUEST")) {
          ApiResponse.ApiEmptyResponse()
        } else {
          ApiResponse.ApiSuccessResponse(body)
        }
      } else {
        ApiResponse.ApiErrorResponse(parseError(response))
      }
    } catch (e: Exception) {
      ApiResponse.ApiErrorResponse(e.message.toString())
    }
  }

  private fun <T> parseError(response: Response<T>): String {
    val msg = response.errorBody()?.string()
    return (if (msg.isNullOrEmpty()) response.message() else msg) ?: "unknown error"
  }
}
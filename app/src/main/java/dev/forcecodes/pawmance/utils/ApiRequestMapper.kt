package dev.forcecodes.pawmance.utils

import dev.forcecodes.pawmance.api.ApiResponse
import dev.forcecodes.pawmance.api.ApiResponseStatus

fun <T : ApiResponseStatus> ApiResponse<T>.mapApiRequestResults(
  emptyApiResponseMessage: () -> String
): Result<T> {
  return when (this) {
    is ApiResponse.ApiSuccessResponse<T> -> Result.Success(body)
    is ApiResponse.ApiErrorResponse<T> -> Result.Error(Exception(errorMessage))
    is ApiResponse.ApiEmptyResponse<T> -> Result.Error(Exception(emptyApiResponseMessage()))
  }
}

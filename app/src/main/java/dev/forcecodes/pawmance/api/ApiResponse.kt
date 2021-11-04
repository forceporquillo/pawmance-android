package dev.forcecodes.pawmance.api

sealed class ApiResponse<T> {
  data class ApiSuccessResponse<T>(val body: T) : ApiResponse<T>()
  data class ApiErrorResponse<T>(val errorMessage: String) : ApiResponse<T>()
  class ApiEmptyResponse<T> : ApiResponse<T>()
}
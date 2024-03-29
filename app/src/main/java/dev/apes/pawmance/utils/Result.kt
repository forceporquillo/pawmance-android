package dev.apes.pawmance.utils

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out R> {

  data class Success<out T>(val data: T) : Result<T>()
  data class Error(val exception: Exception) : Result<Nothing>()
  object Loading : Result<Nothing>()

  override fun toString(): String {
    return when (this) {
      is Success<*> -> "Success[data=$data]"
      is Error -> "Error[exception=$exception]"
      is Loading -> "Loading"
    }
  }
}

/**
 * `true` if [Result] is of type [Success] & holds non-null [Success.data].
 */
val Result<*>.succeeded
  get() = this is Result.Success && data != null

fun <T> Result<T>.successOr(fallback: T): T {
  return (this as? Result.Success<T>)?.data ?: fallback
}

val <T> Result<T>.data: T?
  get() = (this as? Result.Success)?.data

val <T> Result<T>.error: Exception
  get() = (this as Result.Error).exception

val <T> Result<T>.notNullData: T
  get() = (this as Result.Success).data

/**
 * Updates value of [MutableStateFlow] if [Result] is of type [Success]
 */
inline fun <reified T> Result<T>.updateOnSuccess(stateFlow: MutableStateFlow<T>) {
  if (this is Result.Success) {
    stateFlow.value = data
  }
}
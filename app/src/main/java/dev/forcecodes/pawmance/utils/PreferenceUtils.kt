package dev.forcecodes.pawmance.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

fun Context.getMaxPreferredDistance(): Int {
  val prefs = getLocationSharedPrefs()
  return prefs.getInt("max_distance_search", 35)
}

fun Context.storeMaxLocationPrefs(value: Float) {
  val preference = getLocationSharedPrefs()
  preference.edit()
    .putInt("max_distance_search", value.toInt())
    .apply()
}

fun Context.getLocationSharedPrefs(): SharedPreferences {
  return getSharedPreferences("location_preferences", Context.MODE_PRIVATE)
}

@SuppressLint("MissingPermission")
suspend fun Context.getMyCurrentLocation(): Flow<Location> {
  return flow {
    val lastLocation =
      LocationServices
        .getFusedLocationProviderClient(this@getMyCurrentLocation)
        .lastLocation
        .await()
    emit(lastLocation)
  }
}
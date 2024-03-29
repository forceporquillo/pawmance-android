package dev.apes.pawmance.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun daysOfWeekFromLocale(): Array<DayOfWeek> {
  val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
  var daysOfWeek = DayOfWeek.values()
  // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
  // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
  if (firstDayOfWeek != DayOfWeek.MONDAY) {
    val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
    val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
    daysOfWeek = rhs + lhs
  }
  return daysOfWeek
}
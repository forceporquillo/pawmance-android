package dev.apes.pawmance.utils

import android.annotation.SuppressLint
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("ConstantLocale")
object TimeUtils {

  private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

  private val timeFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
  private val timeFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

  fun instantTimeLocalize(): Long {
    return hereAndNow().toEpochSecond()
  }

  private fun now(): Instant {
    return Instant.now()
  }

  private fun hereAndNow(): ZonedDateTime {
    return ZonedDateTime.ofInstant(now(), ZoneId.systemDefault())
  }

  @JvmStatic
  fun nowToFormattedDate(): String? {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val instant = Instant.ofEpochMilli(instantTimeLocalize())
    val dateTime = instant.atZone(ZoneId.systemDefault())

    return dateTime.format(formatter)
  }

  fun convertDate(timestamp: Long): String? {
    return timeFormat1.format(getCalendarTime(timestamp.toString()).time)
  }

  fun convertDate(timestamp: String?): String? {
    if (timestamp == null) {
      return null
    }

    val calendar = getCalendarTime(timestamp)
    return dateFormat.format(calendar.time)
  }

  fun convertTime(timestamp: String?): String? {
    if (timestamp == null) {
      return null
    }

    val calendar = getCalendarTime(timestamp)
    return timeFormat.format(calendar.time)
  }

  private fun getCalendarTime(timestamp: String?): Calendar {
    return Calendar.getInstance().apply {
      timeInMillis = timestamp?.toLong() ?: 0L
    }
  }
}
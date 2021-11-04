package dev.forcecodes.pawmance.utils

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

object TimeUtils {

  fun instantTimeLocalize(): Long {
    return hereAndNow().toEpochSecond()
  }

  private fun now(): Instant {
    return Instant.now()
  }

  private fun hereAndNow(): ZonedDateTime {
    return ZonedDateTime.ofInstant(now(), ZoneId.systemDefault())
  }
}
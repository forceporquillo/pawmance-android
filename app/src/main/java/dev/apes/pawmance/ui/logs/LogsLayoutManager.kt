package dev.apes.pawmance.ui.logs

import android.content.Context
import android.graphics.PointF
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class LogsLayoutManager constructor(
  private val context: Context
) : LinearLayoutManager(context, VERTICAL, false) {

  companion object {
    private const val MILLIS_PER_INCH = 500f
  }

  override fun smoothScrollToPosition(
    recyclerView: RecyclerView?,
    state: RecyclerView.State?,
    position: Int
  ) {
    val smoothScroller = object : LinearSmoothScroller(context) {
      override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return MILLIS_PER_INCH / displayMetrics.densityDpi
      }

      override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        return this@LogsLayoutManager
          .computeScrollVectorForPosition(position)
      }
    }
    // Space characters in SimpleName '$this_no name provided'
    // are not allowed prior to DEX version 040
    smoothScroller.targetPosition = position
    startSmoothScroll(smoothScroller)
  }
}
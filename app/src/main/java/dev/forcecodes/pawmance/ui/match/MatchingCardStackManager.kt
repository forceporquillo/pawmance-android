package dev.forcecodes.pawmance.ui.match

import android.content.Context
import android.view.animation.LinearInterpolator
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.StackFrom
import com.yuyakaido.android.cardstackview.SwipeableMethod

class MatchingCardStackManager(
  context: Context,
  listener: CardStackListener
) : CardStackLayoutManager(context, listener) {

  init {
    setStackFrom(StackFrom.Bottom)
    setVisibleCount(3)
    setTranslationInterval(8.0f)
    setScaleInterval(0.95f)
    setSwipeThreshold(0.3f)
    setMaxDegree(20.0f)
    setDirections(Direction.HORIZONTAL)
    setCanScrollHorizontal(true)
    setCanScrollVertical(true)
    setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
    setOverlayInterpolator(LinearInterpolator())
  }
}
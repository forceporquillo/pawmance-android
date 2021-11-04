package dev.forcecodes.pawmance.ui.progress

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.CalendarDayBinding
import com.devforcecodes.pawmance.databinding.CalendarLegendDayBinding
import com.devforcecodes.pawmance.databinding.FragmentPetProgressBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.pawmance.binding.viewBinding
import dev.forcecodes.pawmance.ui.profile.PetProfileViewModel
import dev.forcecodes.pawmance.utils.bindImageWith
import dev.forcecodes.pawmance.utils.daysOfWeekFromLocale
import dev.forcecodes.pawmance.utils.navigate
import dev.forcecodes.pawmance.utils.repeatOnLifecycle
import dev.forcecodes.pawmance.utils.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PetProgressFragment : Fragment(R.layout.fragment_pet_progress) {

  private val viewModel by viewModels<PetProfileViewModel>()
  private val binding by viewBinding(FragmentPetProgressBinding::bind)

  private var selectedDate: LocalDate? = null

  private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }

    val date = Calendar.getInstance()

    val currentMonth = YearMonth.from(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())

    val daysOfWeek = daysOfWeekFromLocale()

    binding.exFiveCalendar.setup(
      currentMonth.minusMonths(10),
      currentMonth.plusMonths(10),
      daysOfWeek.first()
    )
    binding.exFiveCalendar.scrollToMonth(currentMonth)

    class DayViewContainer(view: View) : ViewContainer(view) {
      lateinit var day: CalendarDay
      val binding = CalendarDayBinding.bind(view)

      init {
        view.setOnClickListener {
          if (day.owner == DayOwner.THIS_MONTH) {
            if (selectedDate != day.date) {
              val oldDate = selectedDate
              selectedDate = day.date

              val calendarView = this@PetProgressFragment.binding.exFiveCalendar
              calendarView.notifyDayChanged(day)
              oldDate?.let { date -> calendarView.notifyDateChanged(date) }

              navigate(
                R.id.action_petProgressFragment_to_progressUpdateFragment,
                bundleOf("day" to "${day.date}")
              )
            }
          }
        }
      }
    }

    binding.exFiveCalendar.dayBinder = object : DayBinder<DayViewContainer> {
      override fun bind(container: DayViewContainer, day: CalendarDay) {
        container.day = day
        val textView = container.binding.exFiveDayText
        val layout = container.binding.exFiveDayLayout
        textView.text = day.date.dayOfMonth.toString()

        val flightTopView = container.binding.exFiveDayFlightTop
        val flightBottomView = container.binding.exFiveDayFlightBottom
        flightTopView.background = null
        flightBottomView.background = null

        if (day.owner == DayOwner.THIS_MONTH) {
          textView.setTextColor(
            ContextCompat.getColor(
              requireContext(),
              R.color.dracula_bottom_toolbar_preview_text
            )
          )
          layout.setBackgroundResource(if (selectedDate == day.date) R.drawable.selected_background else 0)
        } else {
          textView.setTextColor(
            ContextCompat.getColor(
              requireContext(),
              android.R.color.transparent
            )
          )
          layout.background = null
        }
      }

      override fun create(view: View): DayViewContainer {
        return DayViewContainer(view)
      }
    }

    observePetProfile()

    class MonthViewContainer(view: View) : ViewContainer(view) {
      val legendLayout = CalendarLegendDayBinding.bind(view).legendLayout
    }

    binding.exFiveCalendar.monthHeaderBinder = object :
      MonthHeaderFooterBinder<MonthViewContainer> {
      override fun create(view: View) = MonthViewContainer(view)
      override fun bind(container: MonthViewContainer, month: CalendarMonth) {
        // Setup each header day text if we have not done that already.
        if (container.legendLayout.tag == null) {
          container.legendLayout.tag = month.yearMonth
          container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
            tv.text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
              .uppercase(Locale.ENGLISH)
            tv.setTextColor(
              ContextCompat.getColor(
                requireContext(),
                R.color.cardview_dark_background
              )
            )
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
          }
          month.yearMonth
        }
      }
    }

    binding.exFiveCalendar.monthScrollListener = { month ->
      val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
      binding.monthYearText.text = title

      selectedDate?.let {
        // Clear selection if we scroll to a new month.
        selectedDate = null
        binding.exFiveCalendar.notifyDateChanged(it)
      }
    }
  }

  private fun observePetProfile() {
    repeatOnLifecycle {
      viewModel.petInfo.collect {
        binding.petBreed.text = it?.petBreed()
        binding.petNameMonths.text = it?.petName()

        bindImageWith(binding.petProfile, it?.petPrimaryProfile())
      }
    }
  }
}
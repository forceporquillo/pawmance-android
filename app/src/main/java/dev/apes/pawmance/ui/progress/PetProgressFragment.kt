package dev.apes.pawmance.ui.progress

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.CalendarDayBinding
import com.devforcecodes.pawmance.databinding.CalendarLegendDayBinding
import com.devforcecodes.pawmance.databinding.FragmentPetProgressBinding
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.data.progress.PetProgress
import dev.apes.pawmance.ui.profile.PetProfileViewModel
import dev.apes.pawmance.utils.bindImageWith
import dev.apes.pawmance.utils.daysOfWeekFromLocale
import dev.apes.pawmance.utils.navigate
import dev.apes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@AndroidEntryPoint
class PetProgressFragment : Fragment(R.layout.fragment_pet_progress) {

  private val viewModel by viewModels<PetProfileViewModel>()
  private val binding by viewBinding(FragmentPetProgressBinding::bind)
  private val progressViewModel by viewModels<PetProgressViewModel>()

  private var selectedDate: LocalDate? = null

  private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

  private val dayOfWeeks by lazy { daysOfWeekFromLocale() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }

    getAllProgress()
    observePetProfile()
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

  private fun getAllProgress() {
    repeatOnLifecycle {
      progressViewModel.progress.collect { progressList ->
        reloadViewDayBinderView(progressList ?: return@collect)
      }
    }
  }

  private fun reloadViewDayBinderView(progressList: List<PetProgress>) {
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
          val progress = progressList.find { it.day == day.date.toString() }

          if (progress != null) {
            flightBottomView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
          }

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

    binding.exFiveCalendar.monthHeaderBinder = object :
      MonthHeaderFooterBinder<MonthViewContainer> {
      override fun create(view: View) = MonthViewContainer(view)
      override fun bind(container: MonthViewContainer, month: CalendarMonth) {
        // Setup each header day text if we have not done that already.
        if (container.legendLayout.tag == null) {
          container.legendLayout.tag = month.yearMonth
          container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
            tv.text = dayOfWeeks[index].getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
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

    val date = Calendar.getInstance()

    val currentMonth = YearMonth.from(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())

    binding.exFiveCalendar.setup(
      currentMonth.minusMonths(10),
      currentMonth.plusMonths(10),
      dayOfWeeks.first()
    )

    binding.exFiveCalendar.scrollToMonth(currentMonth)

  }

  inner class MonthViewContainer(view: View) : ViewContainer(view) {
    val legendLayout = CalendarLegendDayBinding.bind(view).legendLayout
  }

  inner class DayViewContainer(view: View) : ViewContainer(view) {
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

}
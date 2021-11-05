package dev.apes.pawmance.ui.progress

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentProgressUpdateBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ProgressUpdateFragment : Fragment(R.layout.fragment_progress_update) {

  private val binding by viewBinding(FragmentProgressUpdateBinding::bind)
  private val progressViewModel by viewModels<PetProgressViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.toolbar.setNavigationOnClickListener {
      navigateUp()
    }

    val day = requireArguments().getString("day")

    repeatOnLifecycle {
      progressViewModel.progressByDay.collect {
        binding.progressEditText.setText(it?.note ?: "")
      }
    }

    binding.progressEditText.addTextChangedListener { progress ->
      progressViewModel.addProgressNote(progress.toString(), day)
    }

    binding.updateStatusButton.setOnClickListener {
      progressViewModel.onUpdateStatusButton()
      navigateUp()
    }

    progressViewModel.getProgressByDay(day ?: return)

  }

  private fun navigateUp() {
    findNavController().navigateUp()
  }
}
package dev.forcecodes.pawmance.ui.progress

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentProgressUpdateBinding
import dev.forcecodes.pawmance.binding.viewBinding

class ProgressUpdateFragment : Fragment(R.layout.fragment_progress_update) {

  private val binding by viewBinding(FragmentProgressUpdateBinding::bind)
  private val progressViewModel by viewModels<PetProgressViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }

    val day = requireArguments().getString("day")

    binding.progressEditText.addTextChangedListener {
      progressViewModel.addProgressNote(it.toString(), day)
    }

    binding.updateStatusButton.setOnClickListener {
      progressViewModel.onUpdateStatusButton()
    }
  }
}
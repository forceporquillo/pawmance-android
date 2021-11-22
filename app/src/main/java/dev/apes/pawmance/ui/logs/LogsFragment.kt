package dev.apes.pawmance.ui.logs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentLogsBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class LogsFragment : Fragment(R.layout.fragment_logs) {

  private val viewModel by viewModels<LogsViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val binding = FragmentLogsBinding.bind(view)

    val logsAdapter = LogsAdapter()

    binding.logList.apply {
      adapter = logsAdapter
      layoutManager = LogsLayoutManager(requireContext())
    }

    binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

    repeatOnLifecycle {
      viewModel.logs.collect(logsAdapter::submitList)
    }
  }
}
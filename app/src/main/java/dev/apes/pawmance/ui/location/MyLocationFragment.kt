package dev.apes.pawmance.ui.location

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentMyLocationBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.utils.createAdapter
import dev.apes.pawmance.utils.navigate
import dev.apes.pawmance.utils.repeatOnLifecycleLaunch
import dev.apes.pawmance.utils.showLoading
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MyLocationFragment : Fragment(R.layout.fragment_my_location) {

  private val myLocationViewModel by viewModels<MyLocationViewModel>()
  private val binding by viewBinding(FragmentMyLocationBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.enterLocationEditText.apply {
      setOnItemClickListener { _, _, index, _ ->
        Timber.e("setOnItemClickListener")
        val prediction = myLocationViewModel.placesResults.value[index]
        myLocationViewModel.getMyLocationData(prediction)
      }

      addTextChangedListener { query ->
        Timber.e("addTextChangedListener $query")
        myLocationViewModel.searchPlace(query.toString())
      }

      repeatOnLifecycleLaunch {
        launch {
          myLocationViewModel.placesResults.collectLatest { predictions ->
            val queryAdapter =
              setQueryAdapter(predictions.map { it?.description ?: "" })
            setAdapter(queryAdapter)
            showDropDown()
          }
        }

        launch { myLocationViewModel.addPetUiState.collect { showLoading(it.isLoading) } }
        launch {
          myLocationViewModel.enableSubmit.collect {
            binding.continueButton.isEnabled = it
          }
        }
      }
    }

    binding.continueButton.setOnClickListener {
      navigate(R.id.action_myLocationFragment_to_petPhotosFragment)
    }
  }

  private fun setQueryAdapter(items: List<String>) = createAdapter(items.toTypedArray())
}
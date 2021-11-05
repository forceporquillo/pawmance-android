package dev.apes.pawmance.ui.profile

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentProfileBinding
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.filter.Filter
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.ui.photos.PetPhotosFragment
import dev.apes.pawmance.utils.bindImageWith
import dev.apes.pawmance.utils.navigate
import dev.apes.pawmance.utils.repeatOnLifecycleLaunch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val REQUEST_CODE_CHOOSE = 0x1377

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

  private val viewModel by viewModels<PetProfileViewModel>()
  private val binding by viewBinding(FragmentProfileBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    repeatOnLifecycleLaunch {
      launch {
        viewModel.petInfo.collect {
          binding.name.text = it?.petName()
          binding.breedType.text = it?.petBreed()
          binding.location.text = it?.location()?.placeName
        }
      }

      launch {
        viewModel.uploadTask.collect {
          if (it?.isSuccess == true && it.data != null) {
            setProfileImage(it.data)
          }
        }
      }
    }

    binding.chatsButton.setOnClickListener {
      navigate(R.id.action_profileFragment_to_chatFragment)
    }

    binding.settings.setOnClickListener {
      navigate(R.id.action_profileFragment_to_settingsFragment)
    }

    binding.addPhotoIcon.setOnClickListener { showGalleryPicker() }

    binding.findMatchNow.setOnClickListener {
      navigate(R.id.action_profileFragment_to_petProgressFragment)
    }

    binding.findMatchNow1.setOnClickListener {
      navigate(R.id.action_profileFragment_to_matchPetFragment)
    }

    binding.editInfo.setOnClickListener {
      navigate(R.id.action_profileFragment_to_editPetInfoFragment)
    }
  }

  override fun onResume() {
    super.onResume()
    repeatOnLifecycleLaunch {
      launch {
        viewModel.userInfo.collect {
          it?.getPhotoUrl()?.let { uri ->
            setProfileImage(uri)
          }
        }
      }
    }
  }

  private fun setProfileImage(uri: Uri?) {
    binding.profileAvatar.post { bindImageWith(binding.profileAvatar, uri) }
  }

  private fun showGalleryPicker() {
    Matisse.from(this)
      .choose(MimeType.ofAll())
      .addFilter(PetPhotosFragment.GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
      .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
      .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
      .thumbnailScale(0.85f)
      .imageEngine(GlideEngine())
      .showPreview(true) // Default is `true`
      .maxOriginalSize(10)
      .originalEnable(true)
      .theme(R.style.PickerGalleryStyle)
      .autoHideToolbarOnSingleTap(true)
      .forResult(REQUEST_CODE_CHOOSE)
  }

  @Suppress("deprecation")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
      val imageUri = Matisse.obtainResult(data)
      setProfileImage(imageUri[0])
      viewModel.addPetProfile(imageUri[0])
    }
  }
}
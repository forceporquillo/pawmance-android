package dev.forcecodes.pawmance.ui.photos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentPetPhotosBinding
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.IncapableCause
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.pawmance.binding.viewBinding
import dev.forcecodes.pawmance.ui.registration.AddPetDetailsUiEvent
import dev.forcecodes.pawmance.utils.bindImageWith
import dev.forcecodes.pawmance.utils.navigate
import dev.forcecodes.pawmance.utils.repeatOnLifecycleLaunch
import dev.forcecodes.pawmance.utils.showLoading
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

private const val REQUEST_CODE_CHOOSE = 0x1435
private const val SELECTED_INDEX = "selected_index"

@AndroidEntryPoint
class PetPhotosFragment : Fragment(R.layout.fragment_pet_photos) {

  private val binding by viewBinding(FragmentPetPhotosBinding::bind)
  private val viewModel by viewModels<PetPhotosViewModel>()

  private var selectedIndex = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.viewmodel = viewModel
    binding.lifecycleOwner = viewLifecycleOwner

    galleryIcons().forEachIndexed { index, imageView ->
      imageView.setOnClickListener {
        showGalleryPicker(index)
      }
    }

    photoViews().forEachIndexed { index, imageView ->
      imageView.setOnClickListener {
        showGalleryPicker(index)
      }
    }

    repeatOnLifecycleLaunch {
      launch {
        viewModel.addPetUiState.collect {
          showLoading(it.isLoading)
        }
      }
      launch {
        viewModel.petPhotosNavAction.collect { uiEvent ->
          if (uiEvent is AddPetDetailsUiEvent.ProceedActionEvent) {
            navigate(R.id.action_petPhotosFragment_to_petPreferenceFragment)
          }
        }
      }
    }

    selectedIndex = savedInstanceState?.getInt(SELECTED_INDEX) ?: 0
  }

  @Suppress("deprecation")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
      val imageUri = Matisse.obtainResult(data)
      for (s in Matisse.obtainPathResult(data)) {
        Timber.e("Path $s")
        Timber.e(Uri.fromFile(File(s)).toString())
      }

      val imageView = photoViews()

      var imageDelegate: ImageView

      if (selectedIndex == 0) {
        for ((index, uri) in imageUri.withIndex()) {
          imageDelegate = imageView[index]
          bindImageWith(imageDelegate, uri)
          storeBitmapImage(imageDelegate, index)
        }
      } else {
        imageDelegate = imageView[selectedIndex]
        bindImageWith(imageDelegate, imageUri[0])
        storeBitmapImage(imageDelegate, selectedIndex)
      }
    }
  }

  private fun storeBitmapImage(imageView: ImageView, index: Int) {
    viewModel.addBitmapImage(imageView.toBitmap(), index)
  }

  @Suppress("deprecation")
  private fun ImageView.toBitmap(): ByteArray {
    isDrawingCacheEnabled = true
    buildDrawingCache()

    val bitmap = (drawable as? BitmapDrawable)?.bitmap

    val baos = ByteArrayOutputStream()
    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)

    return baos.toByteArray()
  }

  private fun showGalleryPicker(index: Int) {
    selectedIndex = index
    Matisse.from(this)
      .choose(MimeType.ofAll())
      .countable(index == 0)
      .maxSelectable(if (index == 0) 4 else 1)
      .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
      .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
      .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
      .thumbnailScale(0.85f)
      .imageEngine(GlideEngine())
      .showPreview(true) // Default is `true`
      .maxOriginalSize(10)
      .originalEnable(true)
      .theme(R.style.Matisse_Dracula)
      .autoHideToolbarOnSingleTap(true)
      .forResult(REQUEST_CODE_CHOOSE)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(SELECTED_INDEX, selectedIndex)
  }

  internal class GifSizeFilter(
    private val mMinWidth: Int,
    private val mMinHeight: Int,
    private val mMaxSize: Int
  ) : Filter() {
    public override fun constraintTypes(): HashSet<MimeType?> {
      return object : HashSet<MimeType?>() {
        init {
          add(MimeType.GIF)
        }
      }
    }

    override fun filter(context: Context, item: Item): IncapableCause? {
      if (!needFiltering(context, item)) return null
      val size: Point =
        PhotoMetadataUtils.getBitmapBound(context.contentResolver, item.contentUri)
      return if (size.x < mMinWidth || size.y < mMinHeight || item.size > mMaxSize) {
        IncapableCause(
          IncapableCause.DIALOG,
          context.getString(
            R.string.error_gif,
            mMinWidth,
            PhotoMetadataUtils.getSizeInMB(mMaxSize.toLong()).toString()
          )
        )
      } else null
    }
  }

  private fun galleryIcons() = binding.run {
    arrayOf(galleryIcon1, galleryIcon2, galleryIcon3, galleryIcon4)
  }

  private fun photoViews() = binding.run {
    arrayOf(photo1, photo2, photo3, photo4)
  }
}
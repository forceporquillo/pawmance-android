package dev.apes.pawmance.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.devforcecodes.pawmance.R
import dev.apes.pawmance.ui.registration.BaseRegistrationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0

fun AppCompatActivity.repeatOnLifecycle(block: suspend () -> Unit) {
  lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
      block()
    }
  }
}

fun Fragment.repeatOnLifecycle(block: suspend () -> Unit) {
  viewLifecycleOwner.lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
      block()
    }
  }
}

fun Fragment.repeatOnLifecycleLaunch(block: CoroutineScope.() -> Unit) {
  viewLifecycleOwner.lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
      block()
    }
  }
}

fun Fragment.navigate(@IdRes destinationId: Int, bundle: Bundle? = null) {
  findNavController().navigate(destinationId, bundle)
}

fun Fragment.showLoading(show: Boolean) {
  (requireActivity() as BaseRegistrationActivity)
    .showProgress(show)
}

fun <E> SendChannel<E>.tryOffer(element: E): Boolean = try {
  trySend(element).isSuccess
} catch (t: Throwable) {
  false // Ignore
}

/**
 * Cancel the Job if it's active.
 */
fun Job?.cancelIfActive() {
  if (this?.isActive == true) {
    cancel()
  }
}

private const val StopTimeoutMillis: Long = 5000

/**
 * A [SharingStarted] meant to be used with a [StateFlow] to expose data to a view.
 *
 * When the view stops observing, upstream flows stay active for some time to allow the system to
 * come back from a short-lived configuration change (such as rotations). If the view stops
 * observing for longer, the cache is kept but the upstream flows are stopped. When the view comes
 * back, the latest value is replayed and the upstream flows are executed again. This is done to
 * save resources when the app is in the background but let users switch between apps quickly.
 */
val WhileViewSubscribed: SharingStarted = SharingStarted.WhileSubscribed(StopTimeoutMillis)

fun Context.fromHtmlWithParams(@StringRes stringRes: Int, parameter: String? = null): Spanned {
  val stringText = if (parameter.isNullOrEmpty()) {
    this.getString(stringRes)
  } else {
    this.getString(stringRes, parameter)
  }
  return Html.fromHtml(stringText, Html.FROM_HTML_MODE_COMPACT)
}

fun String?.isValidPhoneNumber(stateFlow: MutableStateFlow<String>) {
  if (this?.isEmpty() == true) {
    stateFlow.value = ""
    return
  }
  if (this?.startsWith("9") == false) {
    stateFlow.value = "Invalid phone number."
  }
}

fun FragmentActivity.createIntent(kClazz: KClass<out AppCompatActivity>, finish: Boolean = true) {
  startActivity(Intent(this, kClazz.java))
  if (finish) {
    finish()
  }
}

fun Fragment.createIntent(kClazz: KClass<out AppCompatActivity>) {
  requireActivity().createIntent(kClazz)
}

fun Fragment.createAdapter(items: Array<String>): ArrayAdapter<String?> {
  return ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup, items)
}

fun EditText.textChangeListener(string: KMutableProperty0<String?>) {
  addTextChangedListener {
    val text = it?.toString()
    string.set(text)
  }
}

fun EditText.textChangeListener(block: (String) -> Unit) {
  addTextChangedListener {
    val text = it?.toString()
    if (text != null) {
      block(text)
    }
  }
}

fun Fragment.showToast(message: String) {
  Toast.makeText(
    requireContext().applicationContext,
    message,
    Toast.LENGTH_SHORT
  ).show()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any?> bindImageWith(
  view: ImageView,
  data: T?,
  block: ((Boolean) -> Unit) = {}
) {
  Glide.with(view.context)
    .asBitmap()
    .override(view.width, view.height)
    .load(data)
    .listener(object : RequestListener<Bitmap> {
      override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Bitmap>?,
        isFirstResource: Boolean
      ): Boolean {
        block.invoke(false)
        return false
      }

      override fun onResourceReady(
        resource: Bitmap?,
        model: Any?,
        target: Target<Bitmap>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
      ): Boolean {
        block.invoke(true)
        return false
      }
    })
    .into(view)
}
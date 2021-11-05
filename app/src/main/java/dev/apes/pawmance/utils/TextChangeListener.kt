package dev.apes.pawmance.utils

import android.text.Editable
import android.text.TextWatcher

abstract class TextChangeListener : TextWatcher {
  override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
  override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
    onTextChanged(charSequence)
  }

  override fun afterTextChanged(editable: Editable) {}
  abstract fun onTextChanged(charSequence: CharSequence?)
}
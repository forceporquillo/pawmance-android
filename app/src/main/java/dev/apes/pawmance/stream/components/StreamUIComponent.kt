package dev.apes.pawmance.stream.components

import android.view.View
import androidx.lifecycle.LifecycleOwner
import dev.apes.pawmance.stream.components.StreamComponents

/**
 * Stream UI components for abstracting the implementations of the bindings between
 * ViewModels and Stream UI components.
 */
interface StreamUIComponent {

    val lifecycleOwner: LifecycleOwner

    @StreamComponents
    fun bindLayout(view: View)
}

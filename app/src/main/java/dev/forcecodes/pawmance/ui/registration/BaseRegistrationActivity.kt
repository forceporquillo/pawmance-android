package dev.forcecodes.pawmance.ui.registration

import android.os.Bundle
import androidx.annotation.NavigationRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isInvisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.ActivityRegistrationBinding
import dev.forcecodes.pawmance.binding.viewBinding

abstract class BaseRegistrationActivity(
  @NavigationRes private val graphResId: Int
) : AppCompatActivity() {

  private val binding by viewBinding(ActivityRegistrationBinding::inflate)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(binding.root)
    setToolbar(binding.toolbar)

    navController.setGraph(graphResId)
    binding.toolbar.setupWithNavController(navController)
  }

  private val navController: NavController
    get() {
      return (supportFragmentManager.findFragmentById(
        R.id.nav_host_fragment
      ) as NavHostFragment).navController
    }

  private fun setToolbar(toolbar: Toolbar) {
    setSupportActionBar(toolbar)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    supportActionBar!!.setDisplayShowTitleEnabled(false)
  }

  override fun onSupportNavigateUp(): Boolean {
    return navController.navigateUp() || super.onSupportNavigateUp()
  }

  fun showProgress(show: Boolean) {
    binding.linearProgress.isInvisible = !show
  }
}

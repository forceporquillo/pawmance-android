package dev.apes.pawmance.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentSymptomsBinding
import com.devforcecodes.pawmance.databinding.SymptomsItemsBinding
import dev.apes.pawmance.ui.progress.SymptomsAdapter.SymptomsViewHolder
import io.karn.notify.Notify

class SymptomsFragment : Fragment(R.layout.fragment_symptoms) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val adapter = SymptomsAdapter()


    val binding = FragmentSymptomsBinding.bind(view)
    binding.symptomsList.adapter = adapter
    binding.symptomsList.addItemDecoration(DividerItemDecoration(requireContext(), 1))

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }

    adapter.submitList(
      listOf(
        Symptoms(
          "Changes in Appetite",
          "She may eat less in the beginning or middle of the pregnancy. She may, however, consume more than normal and be unhappy with her meals. These changes are caused by your dog's shifting hormones."
        ),
        Symptoms(
          "Slightly enlarged nipples",
          "While a female dog's nipples are generally small, during the early stages of pregnancy, her nipples swell in size. In comparison to their natural flatness, the areolas also grow rounder."
        ),
        Symptoms(
          "Clear vaginal discharge",
          "The majority of the discharge will be expelled in the first two weeks, although tiny amounts may be seen in the next four to six weeks. Blood in the discharge after the first week is unusual, so contact your veterinarian if you notice any. You should also check your dog's mammary glands on a daily basis"
        ),
        Symptoms(
          "Decreased physical activity",
          "Nesting, mothering activity, restlessness, decreased interest in physical exercise, and even hostility are some of the behavioral changes associated with pseudo-pregnancy."
        ),
        Symptoms(
          "Morning sickness",
          "Morning sickness affects dogs in the same way it affects humans, and it might keep them from eating during the first few weeks of pregnancy."
        )
      )
    )

    Notify
      .with(requireContext())
      .asBigText { // this: Payload.Content.BigText
        // The title of the notification.
        title = "Changes in Appetite \uD83D\uDC36\uD83E\uDDB4"
        // The second line of the (collapsed) notification.
        text = "Monthly Symptoms Update!"
        // The second line of the expanded notification.
        expandedText = "She may eat less in the beginning or middle of the pregnancy. She may, however, consume more than normal and be unhappy with her meals. These changes are caused by your dog's shifting hormones."
        bigText = ""
      }
      .show()

    Notify
      .with(requireContext())
      .asBigText { // this: Payload.Content.BigText
        // The title of the notification.
        title = "Slightly enlarged nipples \uD83D\uDC36\uD83E\uDDB4"
        // The second line of the (collapsed) notification.
        text = "Monthly Symptoms Update!"
        // The second line of the expanded notification.
        expandedText = "While a female dog's nipples are generally small, during the early stages of pregnancy, her nipples swell in size. In comparison to their natural flatness, the areolas also grow rounder."
        bigText = ""
      }
      .show()

  }
}

data class Symptoms(
  val title: String,
  val description: String
)

class SymptomsAdapter : ListAdapter<Symptoms, SymptomsViewHolder>(COMPARATOR) {

  inner class SymptomsViewHolder(
    private val binding: SymptomsItemsBinding
  ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: Symptoms) {
      binding.title.text = data.title
      binding.description.text = data.description
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomsViewHolder {
    return SymptomsViewHolder(
      SymptomsItemsBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )
  }

  override fun onBindViewHolder(holder: SymptomsViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  companion object {
    private val COMPARATOR = object : DiffUtil.ItemCallback<Symptoms>() {
      override fun areItemsTheSame(oldItem: Symptoms, newItem: Symptoms): Boolean {
        return oldItem.title == newItem.title
      }

      override fun areContentsTheSame(oldItem: Symptoms, newItem: Symptoms): Boolean {
        return oldItem == newItem
      }
    }
  }
}
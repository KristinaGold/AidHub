package com.example.aidhub.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aidhub.R
import com.example.aidhub.databinding.ItemWelcomePageBinding


class WelcomeAdapter(
    private val onPermissionRequest: (Int) -> Unit, private val onSkipPressed: (Int) -> Unit
) : RecyclerView.Adapter<WelcomeAdapter.WelcomeViewHolder>() {

    private val pages = listOf(
        WelcomePage(
            "Help nearby",
            "Allow AidHub to access your location.\nThis way we can show you nearby help requests.",
            R.raw.location,
            0
        ),
        WelcomePage(
            "Real-Time Updates",
            "Get notified every time your skill set is needed or someone else is ready to help you.",
            R.raw.notification_bell,
            1
        ),
        WelcomePage(
            "Congratulations",
            "We finished setting up your account",
            R.raw.congratulations,
            2
        )
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WelcomeViewHolder {
        val binding =
            ItemWelcomePageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WelcomeViewHolder(binding)

    }

    override fun onBindViewHolder(holder: WelcomeViewHolder, position: Int) {
        val page = pages[position]
        holder.bind(page)
    }

    override fun getItemCount() = pages.size

    inner class WelcomeViewHolder(private val binding: ItemWelcomePageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val btnTurnOn = binding.btnTurnOn
        val btnSkip = binding.btnSkip

        fun bind(page: WelcomePage) {
            val title = binding.txtPageTitle
            val desc = binding.txtPageDescription
            val image = binding.animationView




            title.text = page.title
            desc.text = page.description
            if (page.type == 2) {
                btnTurnOn.text = "Get Started!"
                btnSkip.visibility = ViewGroup.GONE
            }
            image.setAnimation(page.imageRes)
            image.playAnimation()

            btnTurnOn.setOnClickListener { onPermissionRequest(page.type) }
            btnSkip.setOnClickListener { onSkipPressed(page.type) }
        }
    }
}

data class WelcomePage(val title: String, val description: String, val imageRes: Int, val type: Int)
package com.example.aidhub.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.aidhub.R
import com.example.data.dataStractures.Skill
import com.example.data.dataStractures.Status
import com.google.android.material.chip.Chip

object ChipBuilder {

    fun createSkillChip(
        context: Context,
        skillName: String,
        isCheckable: Boolean = true,
        initiallyChecked: Boolean = false,
        isClickable: Boolean = true,
        isPost: Boolean = false,
        onCheckedChange: ((String, Boolean) -> Unit)? = null
    ): Chip {

        val inflater = LayoutInflater.from(context)
        val chip = inflater.inflate(R.layout.layout_custom_chip, null) as Chip
        val skill = Skill.fromDisplayName(skillName)

        chip.apply {
            text = skill.displayName
            this.isCheckable = isCheckable
            isChecked = if (isPost)
                true
            else
                initiallyChecked
            this.isClickable = isClickable


            chipIcon = ResourcesCompat.getDrawable(resources, skill.iconResId, null)
            chipIconTint = ContextCompat.getColorStateList(context, R.color.chip_text_state)
            chipBackgroundColor =
                ContextCompat.getColorStateList(context, R.color.chip_background_state)
            setTextColor(ContextCompat.getColorStateList(context, R.color.chip_text_state))
            setChipStrokeColorResource(R.color.primary)


            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val marginInDp = 8
            val marginInPx = (marginInDp * context.resources.displayMetrics.density).toInt()

            params.setMargins(marginInPx, 0, marginInPx, 0) // left, top, right, bottom
            layoutParams = params

            chipStrokeWidth = if (isPost) 4f else 0f
            elevation = if (isPost) 0f else 6f
            isCheckedIconVisible = false

            setOnCheckedChangeListener { _, isChecked ->
                //chipStrokeWidth = if (isChecked) 0f else 4f
                // elevation = if (isChecked) 6f else 0f
                onCheckedChange?.invoke(skill.displayName, isChecked)
            }
        }
        return chip
    }


    fun createStatusChip(context: Context, chipDisplayName: String): Chip {

        val inflater = LayoutInflater.from(context)
        val chip = inflater.inflate(R.layout.layout_custom_chip, null) as Chip

        chip.apply {
            text = chipDisplayName
            isCheckable = false
            isChecked = false

            when (chipDisplayName) {
                Status.OPEN.displayName -> {
                    setChipBackgroundColorResource(R.color.status_open_background)
                }

                Status.IN_PROGRESS.displayName -> {
                    setChipBackgroundColorResource(R.color.status_in_progress_background)
                }

                Status.COMPLETED.displayName -> {
                    setChipBackgroundColorResource(R.color.status_closed_background)
                }

                Status.CANCELLED.displayName -> {
                    setChipBackgroundColorResource(R.color.status_cancelled_background)
                    setChipStrokeColorResource(R.color.status_cancelled_stroke)
                }

                "URGENT" -> {
                    chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_urgent, null)
                    chipIconTint = ContextCompat.getColorStateList(context, R.color.white)
                    setChipBackgroundColorResource(R.color.red)
                    setTextColor(ContextCompat.getColorStateList(context, R.color.white))
                    setChipStrokeColorResource(R.color.status_cancelled_stroke)
                }

            }
            setTextColor(ContextCompat.getColor(context, R.color.white))
            chipStrokeWidth = 0f
            isCheckedIconVisible = false
            isClickable = false
            elevation = 6f
            textSize = 12f

        }
        return chip
    }
}
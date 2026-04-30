package com.example.aidhub.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aidhub.managers.AuthManager
import com.example.data.dataStractures.Review
import com.example.aidhub.databinding.ItemReviewBinding
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.utilities.loadProfileImage

class ReviewAdapter(private val userReviews: List<Review?>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = userReviews[position]
        holder.bind(review!!)
    }

    override fun getItemCount(): Int {
        return userReviews.size
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {

            if (review.review.isNotEmpty())
                binding.txtReviewContent.text = review.review
            else
                binding.txtReviewContent.visibility = ViewGroup.GONE

            binding.txtReviewDate.text = DateUtils.getRelativeTimeSpanString(review.timestamp)
            binding.reviewRatingBar.rating = review.rating.toFloat()
            binding.userName.text = review.raterName
            binding.userImage.loadProfileImage(review.raterImageUrl)

            binding.cgTags.removeAllViews()
            val chip = ChipBuilder.createSkillChip(
                binding.root.context,
                review.tag,
                isCheckable = true,
                isClickable = false,
                isPost = true
            )
            binding.cgTags.addView(chip)


        }
    }
}

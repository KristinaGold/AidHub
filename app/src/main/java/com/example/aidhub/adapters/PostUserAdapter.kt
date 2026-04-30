package com.example.aidhub.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aidhub.managers.AnimationManager
import com.example.aidhub.managers.AuthManager
import com.example.data.dataStractures.Post
import com.example.aidhub.databinding.ItemPostUserBinding
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.utilities.loadPostImage
import com.example.aidhub.utilities.loadProfileImage


class PostUserAdapter(
    private val userPosts: List<Post?>,
    private val onDeleteClick: (Post) -> Unit
) :
    RecyclerView.Adapter<PostUserAdapter.PostViewHolder>() {

    private var activeDeleteMenuPosition: Int = -1
    fun closeDeleteMenu() {
        if (activeDeleteMenuPosition != -1) {
            val previousPosition = activeDeleteMenuPosition
            activeDeleteMenuPosition = -1
            notifyItemChanged(previousPosition)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            ItemPostUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = userPosts[position]
        holder.bind(post!!, position == activeDeleteMenuPosition)
    }

    override fun getItemCount(): Int {
        return userPosts.size
    }

    inner class PostViewHolder(private val binding: ItemPostUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val currentUserId = AuthManager.getUid() ?: ""

        fun bind(post: Post, isMenuOpen: Boolean) {
            binding.userName.text = post.userName
            binding.postContent.text = post.content
            binding.userImage.loadProfileImage(post.userImageUrl)
            binding.postTime.text = DateUtils.getRelativeTimeSpanString(post.timestamp)
            binding.txtLikes.text = "${post.likes.size} likes"

            AnimationManager.slideFade(binding.optionsLayout, isMenuOpen, fromTop = true)
            val isMyPost = post.userId == currentUserId
            binding.btnOptions.visibility = if (isMyPost) View.VISIBLE else View.GONE
            if (post.postImageUrl.isNotEmpty()) {
                binding.postImg.visibility = View.VISIBLE
                binding.postImg.loadPostImage(post.postImageUrl)
            } else {
                binding.postImg.visibility = View.GONE
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(post)
                binding.optionsLayout.visibility = View.GONE
            }

            binding.cgTags.removeAllViews()
            post.tags.forEach { tag ->
                val chip = ChipBuilder.createSkillChip(
                    binding.root.context,
                    tag,
                    isCheckable = true,
                    initiallyChecked = false,
                    isClickable = false,
                    isPost = true
                )
                binding.cgTags.addView(chip)
            }

            binding.btnOptions.setOnClickListener {
                val previous = activeDeleteMenuPosition
                activeDeleteMenuPosition = if (activeDeleteMenuPosition == adapterPosition) -1 else adapterPosition
                if (previous != -1) notifyItemChanged(previous)
                notifyItemChanged(adapterPosition)
            }
        }
    }
}
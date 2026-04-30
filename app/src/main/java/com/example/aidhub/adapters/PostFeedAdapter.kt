package com.example.aidhub.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aidhub.managers.AnimationManager
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.R
import com.example.data.dataStractures.Post
import com.example.aidhub.databinding.ItemPostFeedBinding
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.utilities.loadPostImage
import com.example.aidhub.utilities.loadProfileImage

class PostDiffCallback : DiffUtil.ItemCallback<Post?>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
        oldItem.postId == newItem.postId

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
}

class PostAdapter(
    private val onPicClick: (Post) -> Unit,
    private val onLikeClick: (Post) -> Unit,
    private val onContactClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit
) :
    ListAdapter<Post?, RecyclerView.ViewHolder>(PostDiffCallback()) {

    private val VIEW_TYPE_POST = 0
    private val VIEW_TYPE_LOADING = 1
    private var activeDeleteMenuPosition: Int = -1

    fun closeDeleteMenu() {
        if (activeDeleteMenuPosition != -1) {
            val previousPosition = activeDeleteMenuPosition
            activeDeleteMenuPosition = -1
            notifyItemChanged(previousPosition)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) == null) VIEW_TYPE_LOADING else VIEW_TYPE_POST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_POST) {
            val binding =
                ItemPostFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PostViewHolder(binding)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post = getItem(position)
        if (holder is PostViewHolder && post != null) {
            holder.bind(post, position == activeDeleteMenuPosition)
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class PostViewHolder(private val binding: ItemPostFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val currentUserId = AuthManager.getUid() ?: ""

        fun bind(post: Post, isMenuOpen: Boolean) {
            val isMyPost = post.userId == currentUserId

            //basic UI
            binding.userName.text = post.userName
            binding.postContent.text = post.content
            binding.userImage.loadProfileImage(post.userImageUrl)
            binding.postTime.text = DateUtils.getRelativeTimeSpanString(post.timestamp)

            if (post.postImageUrl.isNotEmpty()) {
                binding.postImg.visibility = View.VISIBLE
                binding.postImg.loadPostImage(post.postImageUrl)
            } else {
                binding.postImg.visibility = View.GONE
            }

            updateLikeUI(post, post.likes.contains(currentUserId))
            handleMyPost(isMyPost)
            AnimationManager.slideFade(binding.optionsLayout, isMenuOpen, fromTop = true)
            showSkillTags(post.tags)


            //buttons
            binding.userImage.setOnClickListener { onPicClick(post) }

            binding.btnLike.setOnClickListener {
                onLikeClick(post)
                val isCurrentlyLiked = post.likes.contains(currentUserId)
                if (isCurrentlyLiked) post.likes.remove(currentUserId) else post.likes.add(
                    currentUserId
                )
                updateLikeUI(post, !isCurrentlyLiked)
            }

            binding.btnContact.setOnClickListener {
                binding.btnContact.isClickable = false
                onContactClick(post)
            }

            binding.btnOptions.setOnClickListener {
                val previous = activeDeleteMenuPosition
                activeDeleteMenuPosition =
                    if (activeDeleteMenuPosition == adapterPosition) -1 else adapterPosition
                if (previous != -1) notifyItemChanged(previous)
                notifyItemChanged(adapterPosition)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(post)
                binding.optionsLayout.visibility = View.GONE
            }
        }

        private fun updateLikeUI(post: Post, isLiked: Boolean) {
            binding.btnLike.setIconResource(if (isLiked) R.drawable.ic_heart_red else R.drawable.ic_heart)
            binding.txtLikes.text = "${post.likes.size} likes"
        }


        private fun handleMyPost(isMyPost: Boolean) {
            binding.btnContact.visibility = if (isMyPost) View.GONE else View.VISIBLE
            binding.btnOptions.visibility = if (isMyPost) View.VISIBLE else View.GONE
        }


        private fun showSkillTags(tags: List<String>) {
            binding.cgTags.removeAllViews()
            tags.forEach { tag ->
                val chip = ChipBuilder.createSkillChip(
                    binding.root.context,
                    tag,
                    isCheckable = true,
                    isClickable = false,
                    isPost = true
                )
                binding.cgTags.addView(chip)
            }
        }
    }
}

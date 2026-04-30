package com.example.aidhub.fragments.tabFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aidhub.factories.MyViewModelFactory
import com.example.aidhub.adapters.PostUserAdapter
import com.example.data.dataStractures.DialogType
import com.example.data.dataStractures.Post
import com.example.data.dataStractures.ToastType
import com.example.aidhub.databinding.FragmentUserPostsBinding
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.DialogHelper
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.viewModels.PostViewModel
import com.google.firebase.auth.FirebaseAuth

class UserPostsFragment : Fragment() {


    private var _binding: FragmentUserPostsBinding? = null
    private val binding get() = _binding!!
    private var postList = listOf<Post?>()
    private lateinit var postAdapter: PostUserAdapter
    private val postViewModel: PostViewModel by activityViewModels { MyViewModelFactory() }
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var userId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getString(Constants.USER_ID_KEY) ?: currentUser?.uid
        getUserPosts()


    }

    private fun getUserPosts() {
        postViewModel.getPostsByUser(userId!!)
        postViewModel.userPosts.observe(viewLifecycleOwner) { posts ->

            this.postList = posts
            setupPostRecyclerView()
        }
    }

    private fun setupPostRecyclerView() {
        postAdapter = PostUserAdapter(
            postList,
            onDeleteClick = { post ->
                DialogHelper.showAlertDialog(
                    requireContext(),
                    DialogType.POST_DELETE
                ) { buttonId, _ ->
                    if (buttonId == Constants.BUTTON_POSITIVE_KEY) {
                        postViewModel.deletePost(post.postId)
                        ToastHelper.getInstance().showSuccess(ToastType.POST_DELETED.message)
                        getUserPosts()
                    }
                }
            }
        )

        binding.rvPosts.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(context)
        }

        val itemAnimator = binding.rvPosts.itemAnimator
        if (itemAnimator is DefaultItemAnimator) {
            itemAnimator.supportsChangeAnimations = false
        }

        binding.rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    postAdapter.closeDeleteMenu()
                }
            }
        })

        if (postList.isNotEmpty())
            binding.txtPosts.visibility = View.GONE
        else
            binding.txtPosts.visibility = View.VISIBLE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
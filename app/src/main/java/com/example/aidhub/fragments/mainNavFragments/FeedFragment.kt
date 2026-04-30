package com.example.aidhub.fragments.mainNavFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.factories.MyViewModelFactory
import com.example.aidhub.R
import com.example.aidhub.adapters.FilterAdapter
import com.example.aidhub.adapters.PostAdapter
import com.example.aidhub.fragments.base.BaseFragment
import com.example.data.dataStractures.DialogType
import com.example.data.dataStractures.Post
import com.example.data.dataStractures.ToastType
import com.example.aidhub.databinding.FragmentFeedBinding
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.DialogHelper
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.utilities.hideKeyboard
import com.example.aidhub.viewModels.ChatViewModel
import com.example.aidhub.viewModels.PostViewModel
import com.example.aidhub.viewModels.ProfileViewModel
import kotlin.math.abs

class FeedFragment : BaseFragment<FragmentFeedBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFeedBinding.inflate(inflater, container, false)

    override fun setupTopBar() {
        showNotificationButton()
        showSearchView()
    }

    private val postViewModel: PostViewModel by viewModels { MyViewModelFactory() }
    private val chatViewModel: ChatViewModel by activityViewModels { MyViewModelFactory() }
    private val profileViewModel: ProfileViewModel by activityViewModels { MyViewModelFactory() }
    private lateinit var postAdapter: PostAdapter
    private lateinit var filterAdapter: FilterAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterRecyclerView()
        appBarLayout()
        observePosts()
        observeLoadingState()
        searchPosts()


        if (postViewModel.posts.value.isNullOrEmpty()) {
            postViewModel.loadPosts(isRefresh = true)
        }

        binding.swipeRefresh.setOnRefreshListener {
            postViewModel.isRefreshingData = true
            postViewModel.loadPosts(isRefresh = true)

        }

        profileViewModel.currentUserData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val firstName = user.fullName!!.split(" ")[0]
                binding.txtWelcome.text = "Welcome back, $firstName!"
            }

        }
    }

    private fun searchPosts() {
        binding.topBar.searchViewSkills.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    postViewModel.searchPostsByContent(query.trim())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    postViewModel.searchPostsByContent(newText)
                }
                return true
            }
        })
    }

    private var isSearchCollapsed = false
    private var isManuallyOpened = false
    private fun appBarLayout() {
        binding.appBar.setExpanded(postViewModel.isAppBarExpanded, false)
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            postViewModel.isAppBarExpanded = (verticalOffset == 0)

            if (isManuallyOpened) return@addOnOffsetChangedListener

            val percentage = abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange

            if (percentage > 0.8f && !isSearchCollapsed) {
                isSearchCollapsed = true
                toggleSearchAnimation(true)
                hideKeyboard()
                topBarBinding.searchViewSkills.clearFocus()
            } else if (percentage < 0.2f && isSearchCollapsed) {
                isSearchCollapsed = false
                toggleSearchAnimation(false)
            }
        }

        topBarBinding.searchViewSkills.setOnSearchClickListener {
            isManuallyOpened = true
            isSearchCollapsed = false
            toggleSearchAnimation(false)
        }

        binding.recyclerViewFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (isManuallyOpened) {
                        isManuallyOpened = false
                        isSearchCollapsed = true
                        toggleSearchAnimation(true)
                        hideKeyboard()
                        topBarBinding.searchViewSkills.clearFocus()
                    }
                }
            }
        })
    }

    private fun observePosts() {
        postViewModel.posts.observe(viewLifecycleOwner) { fullList ->
            binding.swipeRefresh.isRefreshing = false

            if (fullList.isNullOrEmpty())
                binding.txtNoPostsFound.visibility = View.VISIBLE
            else
                binding.txtNoPostsFound.visibility = View.GONE

            postAdapter.submitList(fullList.toList())
            if (postViewModel.isRefreshingData) {

                binding.recyclerViewFeed.post {
                    binding.recyclerViewFeed.scrollToPosition(0)
                }
                postViewModel.isRefreshingData = false
            }

            binding.progressBarLayout.visibility = View.GONE
        }
    }

    private fun observeLoadingState() {
        val touchBlocker = object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return true
            }
        }
        postViewModel.isLoadingLiveData.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading && postViewModel.posts.value.isNullOrEmpty()) {
                binding.progressBarLayout.visibility = View.VISIBLE
                binding.recyclerViewFilters.addOnItemTouchListener(touchBlocker)
            } else if (!isLoading) {
                binding.progressBarLayout.visibility = View.GONE
                binding.recyclerViewFilters.removeOnItemTouchListener(touchBlocker)
            }
        }
    }

    private fun loadMore() {
        if (!postViewModel.isLoading && !postViewModel.isLastPageReached) {
            postViewModel.loadPosts(isRefresh = false)
        }
    }

    private fun setupFilterRecyclerView() {
        val recyclerView = binding.recyclerViewFilters
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        filterAdapter = FilterAdapter { selectedFilter ->
            if (selectedFilter == "All") {
                postViewModel.loadPosts(isRefresh = true)
            } else {
                postViewModel.searchPostsBySkill(selectedFilter)
            }
        }


        recyclerView.apply {
            adapter = filterAdapter
            this.layoutManager = layoutManager
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.recyclerViewFeed
        val layoutManager = LinearLayoutManager(context)

        postAdapter = PostAdapter(
            onPicClick = { post -> goToProfile(post.userId) },
            onLikeClick = { post -> updateLike(post) },
            onContactClick = { post -> goToChat(post.userId) },
            onDeleteClick = { post ->
                DialogHelper.showAlertDialog(
                    requireContext(),
                    DialogType.POST_DELETE
                ) { buttonId, _ ->
                    if (buttonId == Constants.BUTTON_POSITIVE_KEY) {
                        postViewModel.deletePost(post.postId)
                        ToastHelper.getInstance().showSuccess(ToastType.POST_DELETED.message)
                    }
                }
            }
        )


        recyclerView.apply {
            adapter = postAdapter
            this.layoutManager = layoutManager
        }

        val itemAnimator = binding.recyclerViewFeed.itemAnimator
        if (itemAnimator is DefaultItemAnimator) {
            itemAnimator.supportsChangeAnimations = false
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount

                if (lastVisible == total - 1) {
                    loadMore()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    postAdapter.closeDeleteMenu()
                }
            }
        })
    }

    private fun goToProfile(userId: String) {
        if (userId != AuthManager.getUid()!!) {
            profileViewModel.setProfilePressedUid(userId)
            val navController = findNavController()
            val bundle = Bundle()
            bundle.putString(Constants.USER_ID_KEY, userId)

            navController.navigate(R.id.action_feedFragment_to_profileFragment, bundle)
        }
    }

    private fun goToChat(userId: String) {
        chatViewModel.getChatRoom(listOf(AuthManager.getUid()!!, userId))
        profileViewModel.getProfile(userId)
        chatViewModel.isLoading.observe(viewLifecycleOwner) {
            if (!it)
                findNavController().navigate(R.id.action_feedFragment_to_chatRoomFragment)

        }
    }

    private fun updateLike(post: Post) {
        postViewModel.updateLikesCount(AuthManager.getUid()!!, post)
    }

}
package com.example.aidhub.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.interfaces.IPostRepository
import com.example.data.ImageStorage
import com.example.data.dataStractures.Post
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PostViewModel(private val postRepository: IPostRepository) : ViewModel() {

    private val _posts = MutableLiveData<MutableList<Post?>>()
    val posts: LiveData<MutableList<Post?>> get() = _posts
    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> get() = _userPosts
    private val _newPostId = MutableLiveData<String>()
    val newPostId: LiveData<String> get() = _newPostId
    val isPublishing = MutableLiveData<Boolean>(false)
    val publishSuccess = MutableLiveData<Boolean>()
    private var lastVisible: DocumentSnapshot? = null
    var isLastPageReached = false
    var isLoading = false
    var isRefreshingData = false
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoadingLiveData: LiveData<Boolean> get() = _isLoading
    var isAppBarExpanded: Boolean = true

    fun loadPosts(isRefresh: Boolean = false) {
        if (isLoading || (isLastPageReached && !isRefresh)) return
        isLoading = true
        _isLoading.value = true

        viewModelScope.launch {
            val currentList = if (isRefresh) mutableListOf() else _posts.value ?: mutableListOf()

            if (isRefresh) {
                lastVisible = null
                isLastPageReached = false
            } else {
                currentList.add(null)
                _posts.value = currentList
            }

            postRepository.getPosts(limit = 5, lastDocument = lastVisible)
                .addOnSuccessListener { snapshot ->
                    viewModelScope.launch {
                        delay(800)

                        if (currentList.isNotEmpty() && currentList.last() == null) {
                            currentList.removeAt(currentList.size - 1)
                        }
                        val newPosts = snapshot.toObjects(Post::class.java)
                        currentList.addAll(newPosts)
                        if (snapshot.documents.isNotEmpty()) {
                            lastVisible = snapshot.documents[snapshot.size() - 1]
                        }
                        if (newPosts.size < 5) isLastPageReached = true

                        _posts.value = currentList
                        _isLoading.postValue(false)
                        isLoading = false
                    }
                }
                .addOnFailureListener {
                    if (currentList.isNotEmpty() && currentList.last() == null) {
                        currentList.removeAt(currentList.size - 1)
                        _posts.value = currentList
                    }
                    _isLoading.postValue(false)
                    isLoading = false
                }
        }
    }

    fun searchPostsBySkill(skill: String) {
        isLoading = true
        _isLoading.value = true
        postRepository.searchPostsBySkill(skill) { postList ->
            if (!postList.isEmpty()) {
                _posts.value = postList.toMutableList()
                _isLoading.postValue(false)
                isLoading = false
            }
            else {
                postRepository.searchPostsBySkillPrefix(skill) { postList ->
                    _posts.value = postList.toMutableList()
                    _isLoading.postValue(false)
                    isLoading = false
                }
            }
        }
    }

    fun searchPostsByContent(str: String) {
        isLoading = true
        _isLoading.value = true
        postRepository.getAllPosts { postList ->
            val filteredList = postList.filter {
                it.content.contains(str, ignoreCase = true) || it.userName.contains(str, ignoreCase = true) || it.tags.any { tag ->
                    tag.contains(str, ignoreCase = true) }
            }
            _posts.value = filteredList.toMutableList()
            _isLoading.postValue(false)
            isLoading = false
        }
    }

    fun getPostsByUser(uid: String) {
        postRepository.getPostsByUser(uid) { postList ->
            _userPosts.value = postList
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(postId) { success ->
                if (success) {
                    val currentList = _posts.value?.toMutableList() ?: mutableListOf()

                    val iterator = currentList.iterator()
                    while (iterator.hasNext()) {
                        val post = iterator.next()
                        if (post?.postId == postId) {
                            iterator.remove()
                            break
                        }
                    }
                    _posts.value = currentList
                }
            }
        }
    }


    fun createNewPost(
        userUid: String,
        userName: String,
        userImage: String,
        content: String,
        selectedTags: List<String>
    ) {

        val newPost = Post(
            userId = userUid,
            userName = userName,
            userImageUrl = userImage,
            content = content,
            timestamp = System.currentTimeMillis(),
            tags = selectedTags
        )
        isPublishing.value = true
        postRepository.createPost(
            newPost,
            onSuccess = { postId ->
                _newPostId.postValue(postId)
                isPublishing.value = false
                publishSuccess.value = true
            },
            onFailure = {
                _newPostId.postValue("")
                isPublishing.value = false
                publishSuccess.value = false
            }
        )
    }

    fun updatePostImage(postId: String, imageUri: Uri) {
        ImageStorage.uploadPostImage(postId, imageUri, onSuccess = { imageUrl ->
            postRepository.updatePostImage(postId, imageUrl, onComplete = { success ->
                isPublishing.value = false
                publishSuccess.value = true
            })
        }, onFailure = { _ ->
            isPublishing.value = false
            publishSuccess.value = false

        }
        )
    }

    fun updateLikesCount(userId: String, post: Post) {
        val isLiked = post.likes.contains(userId)
        postRepository.toggleLike(post.postId, userId, isLiked)

    }
}
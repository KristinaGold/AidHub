package com.example.data.interfaces

import com.example.data.dataStractures.Post
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

interface IPostRepository {

    fun createPost(
        post: Post,
        onSuccess: (postId: String) -> Unit,
        onFailure: (Exception) -> Unit
    )
    fun getPostsByUser(uid: String, callback: (List<Post>) -> Unit)
    fun deletePost(postId: String, onComplete: (Boolean) -> Unit)
    fun getPosts(limit: Long, lastDocument: DocumentSnapshot?): Task<QuerySnapshot>
    fun searchPostsBySkill(skill: String, callback: (List<Post>) -> Unit)
    fun searchPostsBySkillPrefix(searchQuery: String, callback: (List<Post>) -> Unit)
    fun getAllPosts(callback: (List<Post>) -> Unit)
    fun toggleLike(postId: String, currentUserId: String, alreadyLiked: Boolean)
    fun updatePostImage(postId: String, imageUrl: String, onComplete: (Boolean) -> Unit)



}
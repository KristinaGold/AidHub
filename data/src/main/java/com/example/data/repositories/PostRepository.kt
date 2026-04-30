package com.example.data.repositories

import com.example.data.dataStractures.Post
import com.example.data.ImageStorage
import com.example.data.interfaces.IPostRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

internal class PostRepository private constructor() : IPostRepository {

    private val db = FirebaseFirestore.getInstance()
    private val postsCollection = db.collection("Posts")

    companion object {

        @Volatile
        private var instance: PostRepository? = null

        internal fun init(): PostRepository {
            return instance ?: synchronized(this) {
                instance ?: PostRepository().also { instance = it }
            }
        }

        internal fun getInstance(): PostRepository {
            return instance ?: throw IllegalStateException(
                "PostRepository must be initialized by calling init(context) before use."
            )
        }
    }


    override fun createPost(
        post: Post,
        onSuccess: (postId: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val postId = postsCollection.document().id
        val finalPost = post.copy(postId = postId)

        postsCollection.document(postId).set(finalPost)
            .addOnSuccessListener { onSuccess(postId) }
            .addOnFailureListener { e -> onFailure(e) }
    }

    override fun getPostsByUser(uid: String, callback: (List<Post>) -> Unit) {
        postsCollection
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.toObjects(Post::class.java)
                callback(posts)
            }
    }

    override fun deletePost(postId: String, onComplete: (Boolean) -> Unit) {
        ImageStorage.deletePostImage(postId)
        postsCollection.document(postId).delete().addOnSuccessListener {
            onComplete(true)
        }.addOnFailureListener {
            onComplete(false)
        }
    }


    override fun getPosts(limit: Long, lastDocument: DocumentSnapshot?): Task<QuerySnapshot> {
        var query = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }

        return query.get()
    }

    override fun searchPostsBySkill(skill: String, callback: (List<Post>) -> Unit) {

        postsCollection
            .whereArrayContains("tags", skill).orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val filteredList = documents.toObjects(Post::class.java)
                callback(filteredList)
            }
    }

    override fun searchPostsBySkillPrefix(searchQuery: String, callback: (List<Post>) -> Unit) {
        postsCollection.orderBy("tags")
            .startAt(searchQuery)
            .endAt(searchQuery + "\uf8ff")
            .get().addOnSuccessListener { documents ->
                val filteredList = documents.toObjects(Post::class.java)
                callback(filteredList)
            }
    }

    override fun getAllPosts(callback: (List<Post>) -> Unit) {
        postsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
            .get().addOnSuccessListener { documents ->
                val filteredList = documents.toObjects(Post::class.java)
                callback(filteredList)
            }
    }

    override fun toggleLike(postId: String, currentUserId: String, alreadyLiked: Boolean) {
        val postRef = postsCollection.document(postId)

        if (alreadyLiked) {
            postRef.update("likes", FieldValue.arrayRemove(currentUserId))
        } else {
            postRef.update("likes", FieldValue.arrayUnion(currentUserId))
        }
    }


    override fun updatePostImage(postId: String, imageUrl: String, onComplete: (Boolean) -> Unit) {
        postsCollection.document(postId).update("postImageUrl", imageUrl)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
package com.example.data.repositories

import android.util.Log
import com.example.data.dataStractures.Notification
import com.example.data.dataStractures.Profile
import com.example.data.dataStractures.Review
import com.example.data.ImageStorage
import com.example.data.interfaces.IProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

internal class ProfileRepository private constructor(): IProfileRepository {


    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("Users")

    companion object {

        @Volatile
        private var instance: ProfileRepository? = null

        internal fun init(): ProfileRepository {
            return instance ?: synchronized(this) {
                instance ?: ProfileRepository().also { instance = it }
            }
        }

        internal fun getInstance(): ProfileRepository {
            return instance ?: throw IllegalStateException(
                "ProfileRepository must be initialized by calling init(context) before use."
            )
        }

    }

    override fun saveUserProfile(user: Profile, onComplete: (Boolean) -> Unit) {
        usersCollection.document(user.uid!!).set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }


    override fun getUserProfile(uid: String, onSuccess: (Profile?) -> Unit) {
        usersCollection.document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(Profile::class.java)
                onSuccess(user)
            }
    }


    override fun listenToUserProfile(uid: String, onUpdate: (Profile?) -> Unit) {
        usersCollection.document(uid).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val user = snapshot?.toObject(Profile::class.java)
            onUpdate(user)
        }
    }


    override fun updateFields(
        uid: String,
        updates: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        usersCollection.document(uid).update(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }

    }

    override fun updateUserLastLocation(userId: String, userLocation: Map<String, Any>) {
        usersCollection.document(userId)
            .update(userLocation)
            .addOnSuccessListener { Log.d("Location", "User location updated") }
    }

    override fun getUserNotifications(userId: String, onComplete: (List<Notification>) -> Unit) {
        usersCollection.document(userId).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { notification ->
                val notifications = notification.toObjects(Notification::class.java)
                onComplete(notifications)
            }
    }

    override fun getUserReviews(userId: String, onComplete: (List<Review>) -> Unit) {
        usersCollection.document(userId).collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { reviews ->
                val reviews = reviews.toObjects(Review::class.java)
                onComplete(reviews)
            }
    }

    override fun listenToUnreadNotifications(
        userId: String,
        updateNotificationBadge: (Int) -> Unit
    ) {
        usersCollection.document(userId)
            .collection("notifications")
            .whereEqualTo("read", false)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                val unreadCount = value?.size() ?: 0
                updateNotificationBadge(unreadCount)
            }
    }

    override fun updateNotificationRead(userId: String, notificationId: String) {
        usersCollection.document(userId).collection("notifications").document(notificationId)
            .update("read", true)
    }

    override fun addReview(uid: String, review: Review) {
        usersCollection.document(uid).collection("reviews").add(review).addOnSuccessListener {
            Log.d("Review", "Review added")
        }
    }

    override fun updateProfileImage(uid: String, imageUrl: String, onComplete: (Boolean) -> Unit) {
        usersCollection.document(uid).update("profileImageUrl", imageUrl)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    override fun updateNotificationDistances(
        userId: String,
        distance: Double,
        onComplete: (Boolean) -> Unit
    ) {
        usersCollection.document(userId)
            .update("maxRadius", distance).addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    override fun getCurrentMaxRadius(userId: String, onComplete: (Double) -> Unit) {
        usersCollection.document(userId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val distance = snapshot.getDouble("maxRadius")
                if (distance != null) {
                    onComplete(distance)
                }
            }
        }
    }

    override fun deleteProfile(uid: String, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()

        db.collection("Requests")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val imageUrl = doc.getString("requestImageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        val reqId = doc.getString("requestId")
                        ImageStorage.deleteRequestImage(reqId!!)
                    }
                    batch.delete(doc.reference)
                    Log.d("CLEANUP", "Post deleted successfully")

                }
                db.collection("Posts")
                    .whereEqualTo("userId", uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (doc in documents) {
                            val imageUrl = doc.getString("postImageUrl")
                            if (!imageUrl.isNullOrEmpty()) {
                                val postId = doc.getString("postId")
                                ImageStorage.deleteRequestImage(postId!!)
                            }
                            batch.delete(doc.reference)
                            Log.d("CLEANUP", "request deleted successfully")

                        }

                        val userRef = db.collection("Users").document(uid)


                        userRef.collection("notifications").get().addOnSuccessListener { snapshot ->
                            for (doc in snapshot.documents) {
                                batch.delete(doc.reference)
                            }
                            userRef.collection("reviews").get().addOnSuccessListener { snapshot ->
                                for (doc in snapshot.documents) {
                                    batch.delete(doc.reference)
                                }

                                userRef.get().addOnSuccessListener { userDoc ->
                                    val profileImg = userDoc.getString("profileImageUrl")
                                    if (!profileImg.isNullOrEmpty()) {
                                        ImageStorage.deleteProfileImage(uid)
                                    }


                                    batch.delete(userRef)
                                    Log.d("CLEANUP", "user deleted successfully")



                                    batch.commit().addOnSuccessListener {
                                        Log.d("CLEANUP", "Everything deleted successfully")
                                        onComplete(true)
                                    }.addOnFailureListener { e ->
                                        Log.e("CLEANUP", "Error deleting everything", e)
                                        onComplete(false)
                                    }
                                }
                            }
                        }
                    }
            }
    }
}
package com.example.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object ImageStorage {


    private val storageRef = FirebaseStorage.getInstance().reference

    fun uploadProfileImage(
        uid: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef = storageRef.child("profile_images/$uid.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun uploadPostImage(
        postID: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef = storageRef.child("post_images/$postID.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun deletePostImage(postId: String) {
        storageRef.child("post_images/$postId.jpg").delete()
    }
    fun deleteRequestImage(reqId: String) {
        storageRef.child("request_images/$reqId.jpg").delete()
    }
    fun deleteProfileImage(userId: String) {
        storageRef.child("profile_images/$userId.jpg").delete()
    }

    fun uploadRequestImage(
        requestID: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef = storageRef.child("request_images/$requestID.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
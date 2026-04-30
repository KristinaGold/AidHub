package com.example.aidhub.managers

import android.app.Activity
import android.util.Log
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser

object AuthManager {

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getUid(): String? {
        return auth.currentUser?.uid
    }

    fun checkUserStatus(onResult: (UserStatus) -> Unit) {
        if (auth.currentUser == null) {
            onResult(UserStatus.NotLoggedIn)
            return
        }
        auth.currentUser!!.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("AuthManagerTEST", "User is logged in : ${auth.currentUser!!.uid}")
                onResult(UserStatus.LoggedIn(auth.currentUser!!.uid))
            } else {
                val exception = task.exception
                Log.d("AuthManagerTEST", "User not logged in: $exception")
                if (exception is FirebaseAuthInvalidUserException) {
                    auth.signOut()
                    onResult(UserStatus.NotLoggedIn)
                } else {
                    Log.d(
                        "AuthManagerTEST",
                        "User not logged in: $exception , ${auth.currentUser!!.uid}"
                    )
                    onResult(UserStatus.LoggedIn(auth.currentUser!!.uid))
                }
            }
        }
    }

    fun handleSignInResult(
        result: FirebaseAuthUIAuthenticationResult,
        onSuccess: (isNewUser: Boolean) -> Unit,
        onError: (String?) -> Unit
    ) {
        val response = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK) {
            if (response != null && response.isNewUser)
                onSuccess(true)
            else
                onSuccess(false)
        } else {
            onError(response?.error?.localizedMessage)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun reauthenticateAndDelete(password: String, onResult: (Boolean) -> Unit) {
        val email = auth.currentUser?.email

        if (auth.currentUser == null || email == null) {
            onResult(false)
            return
        }
        val credential = EmailAuthProvider.getCredential(email, password)
        auth.currentUser!!.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
            if (reAuthTask.isSuccessful) {

                deleteUser { success ->
                    if (success) {
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                }
            } else {
                onResult(false)
            }
        }
    }


    private fun deleteUser(onResult: (Boolean) -> Unit) {
        auth.currentUser!!.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }


    sealed class UserStatus {
        object NotLoggedIn : UserStatus()
        data class LoggedIn(val uid: String) : UserStatus()
    }
}
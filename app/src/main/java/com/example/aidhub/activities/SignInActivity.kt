package com.example.aidhub.activities

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.factories.MyViewModelFactory
import com.example.aidhub.utilities.Constants
import com.example.aidhub.R
import com.example.aidhub.managers.SettingsManager
import com.example.aidhub.databinding.ActivitySplashBinding
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.utilities.hideSystemBars
import com.example.aidhub.viewModels.ProfileViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val profileViewModel: ProfileViewModel by viewModels { MyViewModelFactory() }
    private val settingsManager = SettingsManager.getInstance()
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemBars(binding.root)
        startLogoAnimation()
        startProgressAnimation()
        settingsManager.applyDarkMode(settingsManager.isDarkModeEnabled())
    }

    private fun startLogoAnimation() {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.2f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.2f)
        ObjectAnimator.ofPropertyValuesHolder(binding.logo, scaleX, scaleY).apply {
            duration = 800
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }

        lifecycleScope.launch {
            delay(1500)
            checkUser()
        }
    }

    private fun startProgressAnimation() {
        val progressBar = binding.loadingProgress
        val progressAnimator = ValueAnimator.ofInt(0, 100)

        progressAnimator.duration = 1500
        progressAnimator.interpolator = LinearInterpolator()

        progressAnimator.addUpdateListener { visualProgress ->
            val animatedValue = visualProgress.animatedValue as Int
            progressBar.progress = animatedValue
        }
        progressAnimator.start()

    }

    private fun checkUser() {
        AuthManager.checkUserStatus { status ->
            when (status) {
                is AuthManager.UserStatus.NotLoggedIn -> startSignIn()
                is AuthManager.UserStatus.LoggedIn -> {
                    profileViewModel.getProfile(status.uid)
                    profileViewModel.otherUserData.observe(this) { userProfile ->
                        if (userProfile == null) startCreateProfileActivity()
                        else startMainActivity()
                    }
                }
            }
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        AuthManager.handleSignInResult(
            result,
            onSuccess = { isNewUser ->
                if (isNewUser) startCreateProfileActivity()
                else startMainActivity()
            },
            onError = { error ->
                ToastHelper.getInstance().showError(error!!)
            }
        )
    }


    private fun startSignIn() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            //AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            // AuthUI.IdpConfig.FacebookBuilder().build(),
            // AuthUI.IdpConfig.TwitterBuilder().build(),
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.logo)
            .setTheme(R.style.Theme_AidHub )
            .setTosAndPrivacyPolicyUrls(
                "https://example.com/terms.html",
                "https://example.com/privacy.html",
            )
            .build()
        signInLauncher.launch(signInIntent)
    }


    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun startCreateProfileActivity() {
        val intent = Intent(this, EditProfileActivity::class.java)
        val bundle = Bundle()
        bundle.putInt(Constants.MODE_KEY, Constants.MODE_SETUP)
        intent.putExtra(Constants.BUNDLE_KEY, bundle)
        startActivity(intent)
        finish()
    }
}
package com.example.aidhub.activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aidhub.viewModels.ProfileViewModel
import com.example.aidhub.R
import com.example.aidhub.databinding.ActivityMainBinding
import com.example.aidhub.utilities.hideSystemBars
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.aidhub.managers.AnimationManager
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.managers.LocationManager
import com.example.aidhub.factories.MyViewModelFactory
import com.example.aidhub.managers.NotificationsManager
import com.example.aidhub.managers.SettingsManager
import com.example.data.dataStractures.DialogType
import com.example.data.dataStractures.NotificationType
import com.example.data.dataStractures.Profile
import com.example.data.dataStractures.Request
import com.example.data.dataStractures.Status
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.DialogHelper
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.viewModels.ChatViewModel
import com.example.aidhub.viewModels.TopBarViewModel
import com.example.aidhub.viewModels.RequestViewModel
import com.example.data.dataStractures.ToastType
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.isInitialized


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val profileViewModel: ProfileViewModel by viewModels { MyViewModelFactory() }
    private val requestViewModel: RequestViewModel by viewModels { MyViewModelFactory() }
    private val chatViewModel: ChatViewModel by viewModels { MyViewModelFactory() }
    private val topBarViewModel: TopBarViewModel by viewModels()
    private lateinit var currentUserData: Profile
    private lateinit var navController: NavController
    private var lastHandledRequestId: String = ""
    private val settingsManager = SettingsManager.getInstance()
    private val notificationManager = NotificationsManager.getInstance()

    private val navOptions = NavOptions.Builder().setEnterAnim(R.anim.from_bottom)
        .setExitAnim(R.anim.to_bottom).setPopEnterAnim(R.anim.from_bottom)
        .setPopExitAnim(R.anim.to_bottom).build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemBars(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigationView) { view, insets ->
            val navigationBarsInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = navigationBarsInsets.bottom)
            insets
        }
        checkUser()
        notificationManager.createNotificationChannel()
        updateLocationInFirestore()
        settingsManager.applyDarkMode(settingsManager.isDarkModeEnabled())
        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController


        handleNotificationIntent(intent)
        setupWithNavController(bottomNavigationView, navController)
        handleUnreadChats(bottomNavigationView)
        profileViewModel.getNumberOfUnreadNotifications(AuthManager.getUid()!!)
        profileViewModel.startListeningToCurrentProfile(AuthManager.getUid()!!)
        observers()
        addButtonListeners()
        bottomNavListener()
        menuLayoutListener()
        destinationListener(navController)
    }


    override fun onPause() {
        super.onPause()
        AnimationManager.toggleAddMenu(binding.btnAdd, binding.addLayoutContainer, false)
    }

    override fun onStart() {
        super.onStart()
        assignTokenToUser()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkUser()
        handleNotificationIntent(intent)
    }

    private fun destinationListener(navController: NavController){
        val fragmentList = listOf(
            R.id.chatRoomFragment,
            R.id.newRequestFragment,
            R.id.newPostFragment,
            R.id.allUserRequestsFragment,
            R.id.notificationsFragment,
            R.id.settingsFragment,
            R.id.aboutFragment,
            R.id.fullScreenImageFragment
        )


        navController.addOnDestinationChangedListener { _, destination, _ ->
            AnimationManager.toggleAddMenu(binding.btnAdd, binding.addLayoutContainer, false)
            showTopRequestButton()
            binding.navWrapper.visibility = View.VISIBLE
            if (destination.id in fragmentList) {
                binding.addLayoutContainer.visibility = View.GONE
                binding.navWrapper.visibility = View.GONE
            }
            if (destination.id == R.id.requestFragment) {
                profileViewModel.currentUserData.observe(this) { user ->
                    if (user == null) return@observe
                    currentUserData = user
                }
            }
        }
    }


    private fun bottomNavListener() {
        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            val currentDestinationId = navController.currentDestination?.id
            val selectedDestinationId = item.itemId
            val navOptions =
                NavOptions.Builder().setEnterAnim(R.anim.fade_in).setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in).setPopExitAnim(R.anim.fade_out)
                    .setPopUpTo(
                        navController.graph.startDestinationId,
                        inclusive = false,
                        saveState = true
                    )
                    .setLaunchSingleTop(true).setRestoreState(true).build()
            if (currentDestinationId != selectedDestinationId) {
                navController.navigate(selectedDestinationId, null, navOptions)
            }
            true
        }

        bottomNavigationView.setOnItemReselectedListener { item ->
            val selectedDestinationId = item.itemId
            navController.navigate(selectedDestinationId, null, navOptions {
                popUpTo(selectedDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            })
        }
    }


    private fun menuLayoutListener() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.topMenuNavView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> navController.navigate(R.id.settingsFragment, null, navOptions)
                R.id.nav_logout -> showLogOutDialog()
                R.id.nav_about -> navController.navigate(R.id.aboutFragment, null, navOptions)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun addButtonListeners() {
        binding.btnAdd.setOnClickListener {
            binding.bottomNavigationView.selectedItemId = R.id.placeholder
            AnimationManager.toggleAddMenu(
                binding.btnAdd,
                binding.addLayoutContainer,
                binding.addLayoutContainer.isGone)
        }
        binding.btnPost.setOnClickListener {
            navController.navigate(R.id.newPostFragment, null, navOptions)
        }
        binding.btnRequest.setOnClickListener {
            navController.navigate(R.id.newRequestFragment, null, navOptions)
        }
    }

    private fun observers() {
        profileViewModel.currentUserData.observe(this) { user ->
            if (user == null) return@observe
            currentUserData = user
        }

        requestViewModel.currentRequestTaken.observe(this) { request ->
            if (request == null || request.requestId.isEmpty()) return@observe
            showRequestCompletedDialog(request)
        }

        profileViewModel.signOutEvent.observe(this) { shouldSignOut ->
            if (shouldSignOut) {
                showLogOutDialog()
                profileViewModel.onSignOutHandled()
            }
        }

        topBarViewModel.requestClickEvent.observe(this) { clicked ->
            if (clicked) {
                topRequestButtonClicked()
                topBarViewModel.onRequestClickEventHandled()
            }
        }

        profileViewModel.numOfUnreadNotifications.observe(this) { numOfNotifications ->
            topBarViewModel.showNotificationBadge(numOfNotifications)

        }
        topBarViewModel.menuClickEvent.observe(this) { clicked ->
            if (clicked) {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                topBarViewModel.onMenuClickEventHandled()
            }

        }
        topBarViewModel.openEditProfile.observe(this) { clicked ->
            if (clicked) {
                openEditProfileActivity()
                topBarViewModel.openEditProfileHandled()
            }
        }
        profileViewModel.deleteEvent.observe(this) { clicked ->
            if (clicked) {
                deleteAccount()
            } else {
                ToastHelper.getInstance().showSuccess(ToastType.PROFILE_DELETED.message)
                startSignInActivity()
            }
        }
    }

    private fun topRequestButtonClicked() {
        val navOptions1 = NavOptions.Builder().setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left).setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right).build()
        val bundle = Bundle()
        bundle.putString(Constants.REQUEST_ID_KEY, currentUserData.currentTakenRequest)
        bundle.putString(Constants.HELPER_ID_KEY, currentUserData.uid)
        navController.navigate(R.id.requestFragment, bundle, navOptions1)
    }


    private fun showTopRequestButton() {
        if (!::currentUserData.isInitialized) return
        if (currentUserData.currentTakenRequest != "") {
            topBarViewModel.showRequestButton()
            requestViewModel.listenToCurrentRequest(currentUserData.currentTakenRequest)
        } else {
            topBarViewModel.doNotShowRequestButton()
        }
    }


    private fun handleUnreadChats(bottomNavigationView: BottomNavigationView) {
        chatViewModel.getNumberOfUnreadChats(AuthManager.getUid()!!)
        val badge: BadgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.chatFragment)
        badge.backgroundColor = ContextCompat.getColor(this, R.color.notification_background)
        badge.badgeTextColor = ContextCompat.getColor(this, R.color.badge_text_color)
        chatViewModel.numOfUnreadChats.observe(this) { numOfChats ->
            if (numOfChats > 0) {
                badge.isVisible = true
                badge.number = numOfChats
            } else {
                badge.isVisible = false
            }
        }
    }

    private fun updateLocationInFirestore() {
        LocationManager.getInstance().getCurrentLocation(
            { lat, lng, _ ->
                profileViewModel.updateLastLocation(AuthManager.getUid()!!, lat, lng)
            },
            {
            })
    }

    private fun checkUser() {
        AuthManager.checkUserStatus { status ->
            if (status == AuthManager.UserStatus.NotLoggedIn) startSignInActivity()
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {

        val bundle = intent!!.getBundleExtra(Constants.BUNDLE_KEY) ?: return

        val notificationId = bundle.getString(Constants.NOTIFICATION_ID_KEY)
        val relatedId = bundle.getString(Constants.RELATED_ID_KEY)
        val type = bundle.getString(Constants.NOTIFICATION_TYPE_KEY)
        val helperId = bundle.getString(Constants.HELPER_ID_KEY)



        if (type != null && relatedId != null) {
            if (type == NotificationType.CHAT.displayName) {
                val senderId = bundle.getString(Constants.SENDER_ID_KEY)
                profileViewModel.getProfile(senderId!!)
                chatViewModel.getChatRoom(listOf(AuthManager.getUid()!!, senderId))
                chatViewModel.updateChatRead(relatedId, AuthManager.getUid()!!)
                navController.navigate(R.id.chatRoomFragment)
            } else if (type == NotificationType.POST_LIKE.displayName) {
                if (notificationId != null)
                    profileViewModel.updateNotificationRead(AuthManager.getUid()!!, notificationId)
            } else {
                val bundle = Bundle()
                bundle.putString(Constants.REQUEST_ID_KEY, relatedId)
                bundle.putString(Constants.HELPER_ID_KEY, helperId)
                if (notificationId != null)
                    profileViewModel.updateNotificationRead(AuthManager.getUid()!!, notificationId)
                navController.navigate(R.id.requestFragment, bundle)
            }
        }

    }


    private fun assignTokenToUser() {
        notificationManager.getToken { token ->
            val userId = AuthManager.getUid()
            if (userId != null)
                profileViewModel.assignToken(userId, token)
        }
    }


    private fun showLogOutDialog() {
        DialogHelper.showAlertDialog(this, DialogType.LOG_OUT) { buttonId, _ ->
            if (buttonId == Constants.BUTTON_POSITIVE_KEY) {
                signOut()
            }
        }
    }

    private fun signOut() {
        AnimationManager.fadeIn(binding.loadingLayout)
        if (AuthManager.getCurrentUser() != null) {
            profileViewModel.deleteToken(AuthManager.getUid()!!)
            profileViewModel.tokenDeleteEvent.observe(this) { success ->
                if (success) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        AuthManager.signOut()
                        startSignInActivity()
                    }, 1800)
                }
            }
        }
    }

    private fun deleteAccount() {
        binding.animationView.setAnimation(R.raw.delete)
        binding.txtSignOut.text = Constants.DELETING_ACCOUNT_KEY
        AnimationManager.fadeIn(binding.loadingLayout)
    }

    private fun startSignInActivity() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    private fun openEditProfileActivity() {
        val intent = Intent(this, EditProfileActivity::class.java)
        val bundle = Bundle()
        bundle.putInt(Constants.MODE_KEY, Constants.MODE_EDIT)
        intent.putExtra(Constants.BUNDLE_KEY, bundle)
        startActivity(intent)
        overridePendingTransition(R.anim.from_bottom, R.anim.from_bottom)

    }


    private fun showRequestCompletedDialog(request: Request) {
        val isStatusFinal = request.status == Status.COMPLETED.displayName ||
                request.status == Status.CANCELLED.displayName

        val isDataReady = if (request.status == Status.COMPLETED.displayName) {
            request.rating > 0 && request.points > 0
        } else {
            true
        }

        if (isStatusFinal && isDataReady && lastHandledRequestId != request.requestId) {
            playSoundEffect(R.raw.inappnotificationsound)
            lastHandledRequestId = request.requestId
            DialogHelper.showRequestCompletedDialog(
                this,
                request.status,
                request.rating,
                request.points
            ) {
                topBarViewModel.doNotShowRequestButton()
                requestViewModel.clearCurrentRequest()
            }
        }
    }

    fun playSoundEffect(soundResourceId: Int) {
        val mediaPlayer = MediaPlayer.create(this, soundResourceId)
        mediaPlayer.setOnCompletionListener { mp ->
            mp.release()
        }
        mediaPlayer.start()
    }
}

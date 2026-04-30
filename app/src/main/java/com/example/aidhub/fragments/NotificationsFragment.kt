package com.example.aidhub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.R
import com.example.aidhub.adapters.NotificationAdapter
import com.example.aidhub.fragments.base.BaseFragment
import com.example.data.dataStractures.Notification
import com.example.data.dataStractures.NotificationType
import com.example.data.dataStractures.ToastType
import com.example.aidhub.databinding.FragmentNotificationsBinding
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.viewModels.ProfileViewModel
import com.example.aidhub.viewModels.RequestViewModel
import kotlin.getValue

class NotificationsFragment : BaseFragment<FragmentNotificationsBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentNotificationsBinding.inflate(inflater, container, false)

    override fun setupTopBar() {
        showBackButton()
        setTopBarTitle(Constants.NOTIFICATIONS_SCREEN_TITLE)
    }

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val requestViewModel: RequestViewModel by activityViewModels()
    private var notificationList = listOf<Notification>()
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeNotifications()
        binding.swipeRefresh.setOnRefreshListener {
            profileViewModel.getUserNotifications(AuthManager.getUid()!!)
            binding.swipeRefresh.isRefreshing = false
        }

    }

    private fun observeNotifications() {
        profileViewModel.getUserNotifications(AuthManager.getUid()!!)
        profileViewModel.notifications.observe(viewLifecycleOwner) { notificationList ->
            this.notificationList = notificationList
            setupRecyclerView()
        }
    }


    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(notificationList) { notification ->
            updateNotificationRead(notification.id)
            if (notification.type != NotificationType.POST_LIKE.displayName) {
                openRequest(notification.relatedId)
            }
        }

        binding.rvNotifications.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun updateNotificationRead(notificationId: String) {
        profileViewModel.updateNotificationRead(AuthManager.getUid()!!, notificationId)

    }


    private fun openRequest(requestId: String) {
        requestViewModel.getCurrentRequest(requestId) { request ->
            if (request != null) {
                val bundle = Bundle()
                bundle.putString(Constants.REQUEST_ID_KEY, requestId)
                bundle.putString(Constants.HELPER_ID_KEY, request.helperId)
                findNavController().navigate(
                    R.id.action_notificationsFragment_to_requestFragment,
                    bundle
                )
            } else
                ToastHelper.getInstance().showError(ToastType.REQUEST_NOT_FOUND.message)
        }
    }
}
package com.example.aidhub.fragments

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.managers.LocationManager
import com.example.aidhub.factories.MyViewModelFactory
import com.example.aidhub.R
import com.example.aidhub.fragments.base.BaseFragment
import com.example.data.dataStractures.DialogType
import com.example.data.dataStractures.Request
import com.example.data.dataStractures.RequestScreenState
import com.example.data.dataStractures.ToastType
import com.example.aidhub.databinding.FragmentRequestBinding
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.utilities.ChipBuilder.createSkillChip
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.DialogHelper
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.utilities.loadPostImage
import com.example.aidhub.utilities.loadProfileImage
import com.example.aidhub.viewModels.ChatViewModel
import com.example.aidhub.viewModels.ProfileViewModel
import com.example.aidhub.viewModels.RequestViewModel
import kotlin.getValue

class RequestFragment : BaseFragment<FragmentRequestBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRequestBinding.inflate(inflater, container, false)

    override fun setupTopBar() {
        showBackButton()
        setTopBarTitle(Constants.REQUEST_SCREEN_TITLE)
    }

    private val requestViewModel: RequestViewModel by activityViewModels { MyViewModelFactory() }
    private val profileViewModel: ProfileViewModel by activityViewModels { MyViewModelFactory() }
    private val chatViewModel: ChatViewModel by activityViewModels { MyViewModelFactory() }
    private var selectedRequestId: String? = ""
    private var helperId: String? = ""
    private lateinit var selectedRequest: Request


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        selectedRequestId = arguments?.getString(Constants.REQUEST_ID_KEY)
        helperId = arguments?.getString(Constants.HELPER_ID_KEY)
        observers()

        //requester
        binding.btnContact.setOnClickListener {
            profileViewModel.getProfile(selectedRequest.userId)
            chatViewModel.getChatRoom(listOf(selectedRequest.userId, AuthManager.getUid()!!))
            findNavController().navigate(R.id.action_requestFragment_to_chatRoomFragment)
        }

        binding.btnProfile.setOnClickListener {
            profileViewModel.setProfilePressedUid(selectedRequest.userId)
            val bundle = Bundle()
            bundle.putString(Constants.USER_ID_KEY, selectedRequest.userId)
            findNavController().navigate(R.id.action_requestFragment_to_profileFragment, bundle)
        }
        //helper
        binding.btnHelperContact.setOnClickListener {
            profileViewModel.getProfile(selectedRequest.helperId)
            chatViewModel.getChatRoom(listOf(selectedRequest.helperId, AuthManager.getUid()!!))
            findNavController().navigate(R.id.action_requestFragment_to_chatRoomFragment)
        }
        binding.btnHelperProfile.setOnClickListener {
            profileViewModel.setProfilePressedUid(selectedRequest.helperId)
            val bundle = Bundle()
            bundle.putString(Constants.USER_ID_KEY, selectedRequest.helperId)
            findNavController().navigate(R.id.action_requestFragment_to_profileFragment, bundle)

        }
        binding.btnHelp.setOnClickListener {
            var isCurrentUserTaken = false
            profileViewModel.currentUserData.observe(viewLifecycleOwner) { user ->
                isCurrentUserTaken = user!!.currentTakenRequest.isEmpty()
            }

            if (isCurrentUserTaken) {
                requestViewModel.acceptRequest(selectedRequest.requestId, AuthManager.getUid()!!)
                profileViewModel.acceptRequest(AuthManager.getUid()!!, selectedRequest.requestId)
                requestViewModel.selectedRequestHelperId.observe(viewLifecycleOwner) {
                    profileViewModel.getProfile(it)
                }
            } else {
                ToastHelper.getInstance().showError(ToastType.HELPER_BUSY.message)
            }
        }

        binding.btnNavigate.setOnClickListener {
            LocationManager.getInstance()
                .navigateToLocation(selectedRequest.latitude, selectedRequest.longitude)
        }

        binding.btnCancel.setOnClickListener {
            DialogHelper.showAlertDialog(
                requireContext(),
                DialogType.CONFIRM_CANCEL_REQUEST,
                { btnId, _ ->
                    if (btnId == Constants.BUTTON_POSITIVE_KEY) {
                        requestViewModel.completeRequest(selectedRequest.requestId, false)
                        if (selectedRequest.helperId.isNotEmpty())
                            profileViewModel.rejectRequest(selectedRequest.helperId)
                    }

                })

        }

        binding.btnClose.setOnClickListener {
            profileViewModel.getProfile(selectedRequest.helperId)
            findNavController().navigate(R.id.action_requestFragment_to_ratingDialogFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        requestViewModel.setSelectedRequest(selectedRequestId!!, AuthManager.getUid()!!)
        if (!helperId.isNullOrEmpty())
            profileViewModel.getProfile(helperId!!)
        observeUIState()
    }


    override fun onPause() {
        super.onPause()
        requestViewModel.clearCurrentRequest()
    }

    private fun observeUIState() {
        requestViewModel.screenState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RequestScreenState.ClosedRequestAuthor -> closedRequestAuthorUI()
                is RequestScreenState.CancelledRequestAuthor -> cancelledRequestAuthorUI()
                is RequestScreenState.ClosedRequestHelper -> closedRequestHelperUI()
                is RequestScreenState.CancelledRequestHelper -> cancelledRequestHelperUI()
                is RequestScreenState.AuthorWaiting -> authorWaitingUI()
                is RequestScreenState.AuthorWithHelper -> authorWithHelperUI()
                is RequestScreenState.IAmTheHelper -> iAmTheHelperUI()
                is RequestScreenState.ViewerCanHelp -> viewerCanHelpUI()
                is RequestScreenState.ViewerRequestBusy -> viewerRequestBusyUI()
            }
        }
    }

    private fun observers() {
        requestViewModel.selectedRequest.observe(viewLifecycleOwner) { request ->
            selectedRequest = request
            updateRequestUI(request)
        }

        profileViewModel.otherUserData.observe(viewLifecycleOwner) { profile ->
            if (profile!!.uid == AuthManager.getUid()!!)
                binding.txtHelperFullName.text = Constants.YOU_KEY
            else
                binding.txtHelperFullName.text = profile.fullName
            binding.imgHelperPicture.loadProfileImage(profile.profileImageUrl)
        }
    }

    private fun authorUI() {
        binding.noHelperLayout.visibility = View.VISIBLE
        binding.txtIAmHelper.visibility = View.GONE
        binding.requestRatingLayout.visibility = View.GONE
        binding.btnHelp.visibility = View.GONE
        binding.layoutContactRequester.visibility = View.GONE
        binding.btnNavigate.visibility = View.GONE
        binding.managePanel.visibility = View.VISIBLE
        binding.layoutContactHelper.visibility = View.VISIBLE
    }

    private fun viewerUI() {
        binding.noHelperLayout.visibility = View.VISIBLE
        binding.requestRatingLayout.visibility = View.GONE
        binding.layoutContactRequester.visibility = View.VISIBLE
        binding.btnNavigate.visibility = View.VISIBLE
        binding.btnHelp.visibility = View.VISIBLE
        binding.txtFirstHelper.visibility = View.VISIBLE
        binding.managePanel.visibility = View.GONE
        binding.layoutContactHelper.visibility = View.VISIBLE
    }

    private fun cancelledRequestAuthorUI() {
        authorUI()
        binding.managePanel.visibility = View.GONE
        binding.helperLayout.visibility = View.GONE
        binding.layoutContactHelper.visibility = View.GONE
        binding.btnNavigate.visibility = View.GONE
    }

    private fun cancelledRequestHelperUI() {
        viewerUI()
        binding.helperLayout.visibility = View.GONE
        binding.requestRatingLayout.visibility = View.GONE
        binding.btnNavigate.visibility = View.GONE

    }

    private fun closedRequestAuthorUI() {
        authorUI()
        binding.helperAssignedLayout.visibility = View.VISIBLE
        binding.requestRatingLayout.visibility = View.VISIBLE
        binding.helperLayout.visibility = View.VISIBLE
        binding.managePanel.visibility = View.GONE
        binding.helperInfo.visibility = View.VISIBLE
        binding.requestRatingLayout.visibility = View.VISIBLE
        binding.btnNavigate.visibility = View.GONE
        binding.noHelperLayout.visibility = View.GONE

    }

    private fun closedRequestHelperUI() {
        iAmTheHelperUI()
        binding.helperAssignedLayout.visibility = View.VISIBLE
        binding.requestRatingLayout.visibility = View.VISIBLE
        binding.helperLayout.visibility = View.VISIBLE
        binding.managePanel.visibility = View.GONE
        binding.helperInfo.visibility = View.VISIBLE
        binding.requestRatingLayout.visibility = View.VISIBLE
        binding.layoutContactHelper.visibility = View.GONE
        binding.btnNavigate.visibility = View.GONE
    }

    private fun authorWaitingUI() {
        authorUI()
        binding.txtCancelRequest.visibility = View.VISIBLE
        binding.txtCloseRequest.visibility = View.GONE
        binding.helperAssignedLayout.visibility = View.GONE
        binding.txtNoHelper.visibility = View.VISIBLE
        binding.btnClose.visibility = View.GONE
        binding.txtFirstHelper.visibility = View.GONE
        binding.btnHelp.visibility = View.GONE
        binding.requestRatingLayout.visibility = View.GONE
        binding.layoutContactHelper.visibility = View.GONE
    }

    private fun authorWithHelperUI() {
        authorUI()
        binding.requestRatingLayout.visibility = View.GONE
        binding.txtCancelRequest.visibility = View.GONE
        binding.txtCloseRequest.visibility = View.VISIBLE
        binding.helperAssignedLayout.visibility = View.VISIBLE
        binding.noHelperLayout.visibility = View.GONE
        binding.btnClose.visibility = View.VISIBLE
        binding.layoutContactHelper.visibility = View.VISIBLE
    }


    private fun iAmTheHelperUI() {
        viewerUI()
        binding.txtIAmHelper.visibility = View.VISIBLE
        binding.helperAssignedLayout.visibility = View.VISIBLE
        binding.layoutContactHelper.visibility = View.GONE
        binding.noHelperLayout.visibility = View.GONE
    }

    private fun viewerCanHelpUI() {
        viewerUI()
        binding.noHelperLayout.visibility = View.VISIBLE
        binding.helperAssignedLayout.visibility = View.GONE
    }

    private fun viewerRequestBusyUI() {
        iAmTheHelperUI()
        binding.txtIAmHelper.visibility = View.GONE
        binding.layoutContactRequester.visibility = View.VISIBLE
        binding.btnNavigate.visibility = View.GONE
        binding.layoutContactHelper.visibility = View.VISIBLE
    }


    private fun updateRequestUI(request: Request) {
        binding.txtFullName.text = request.userName
        binding.imgProfilePicture.loadProfileImage(request.userImageUrl)
        binding.txtTitle.text = request.title
        binding.txtDescription.text = request.content
        binding.txtLocation.text = request.locationInfo
        binding.txtTime.text = DateUtils.getRelativeTimeSpanString(request.timestamp)
        binding.ratingBar.rating = request.rating.toFloat()
        binding.txtReview.text = " \"${request.review}\""
        binding.txtRating.text = request.rating.toString()
        binding.txtReviewTime.text = DateUtils.getRelativeTimeSpanString(request.reviewTimestamp)

        binding.chipTag.removeAllViews()
        binding.chipTag.addView(
            createSkillChip(
                requireContext(),
                request.tag,
                isCheckable = true,
                isPost = true,
                isClickable = false
            )
        )



        if (request.review.isEmpty())
            binding.reviewLayout.visibility = View.GONE
        else
            binding.reviewLayout.visibility = View.VISIBLE


        if (request.requestImageUrl.isNotEmpty()) {
            binding.reqImg.visibility = View.VISIBLE
            binding.reqImg.loadPostImage(request.requestImageUrl)
        } else {
            binding.reqImg.visibility = View.GONE
        }

        binding.reqImg.setOnClickListener {
            openFullScreenImage(request)
        }
        showStatusChip(request.status)

        if (request.urgent) {
            binding.chipTag.addView(
                ChipBuilder.createStatusChip(
                    requireContext(),
                    Constants.URGENT_KEY
                )
            )
        }

    }

    private fun openFullScreenImage(request: Request) {
        val extras = FragmentNavigatorExtras(
            binding.reqImg to "request_image_transition"
        )
        val bundle = bundleOf(Constants.IMAGE_URL_KEY to request.requestImageUrl)
        findNavController().navigate(
            R.id.action_requestFragment_to_fullScreenImageFragment,
            bundle,
            navOptions {
                popUpTo(R.id.requestFragment) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            },
            extras
        )
    }
}

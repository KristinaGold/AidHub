package com.example.aidhub.viewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aidhub.utilities.DialogHelper
import com.example.data.interfaces.IRequestRepository
import com.example.data.dataStractures.Request
import com.example.data.dataStractures.RequestScreenState
import com.example.data.dataStractures.Status
import com.example.data.ImageStorage


class RequestViewModel(private val requestRepository: IRequestRepository) : ViewModel() {

    private val _isPublishing = MutableLiveData<Boolean>(false)
    val isPublishing: LiveData<Boolean> get() = _isPublishing
    private val _publishSuccess = MutableLiveData<Boolean>()
    val publishSuccess: LiveData<Boolean> get() = _publishSuccess
    private val _requests = MutableLiveData<List<Request?>>()
    val requests: LiveData<List<Request?>> get() = _requests
    private val _newRequestId = MutableLiveData<String>()
    val newRequestId: LiveData<String> get() = _newRequestId
    private val _userRequests = MutableLiveData<List<Request?>>()
    val userRequests: LiveData<List<Request?>> get() = _userRequests

    private val _openUserRequests = MutableLiveData<List<Request?>>()
    val openUserRequests: LiveData<List<Request?>> get() = _openUserRequests

    private val _selectedRequest = MutableLiveData<Request>()
    val selectedRequest: LiveData<Request> get() = _selectedRequest

    private val _selectedRequestHelperId = MutableLiveData<String>()
    val selectedRequestHelperId: MutableLiveData<String> get() = _selectedRequestHelperId

    private val _screenState = MutableLiveData<RequestScreenState>()
    val screenState: LiveData<RequestScreenState> get() = _screenState

    private val _currentRequestTaken = MutableLiveData<Request?>()
    val currentRequestTaken: LiveData<Request?> get() = _currentRequestTaken

    private val _showRequestCompletedDialog = MutableLiveData<Boolean>(false)
    val showRequestCompletedDialog: LiveData<Boolean> get() = _showRequestCompletedDialog

    fun setSelectedRequest(reqId: String, currentUserId: String) {
        requestRepository.listenToRequest(reqId) { request ->
            _selectedRequest.value = request!!
            _selectedRequestHelperId.value = request.helperId


            val hasHelper = request.helperId != ""
            val isAuthor = request.userId == currentUserId
            val isHelper = request.helperId == currentUserId
            val isClosed = request.status == Status.COMPLETED.displayName
            val isCancelled = request.status == Status.CANCELLED.displayName


            _screenState.value = if (isClosed && isAuthor) {
                RequestScreenState.ClosedRequestAuthor
            } else if (isCancelled && isAuthor) {
                RequestScreenState.CancelledRequestAuthor
            } else if (isClosed && isHelper) {
                RequestScreenState.ClosedRequestHelper
            } else if (isCancelled && isHelper) {
                RequestScreenState.CancelledRequestHelper
            } else when {
                isAuthor && !hasHelper -> RequestScreenState.AuthorWaiting
                isAuthor && hasHelper -> RequestScreenState.AuthorWithHelper
                isHelper -> RequestScreenState.IAmTheHelper
                !hasHelper -> RequestScreenState.ViewerCanHelp
                else -> RequestScreenState.ViewerRequestBusy
            }
        }
    }

    fun setRequest(obj: Request) {
        _selectedRequest.value = obj
    }

    fun getCurrentRequest(currentRequestId: String, callBack: (Request?) -> Unit) {
        requestRepository.getRequest(currentRequestId) { request ->
            callBack(request)
        }
    }

    fun listenToCurrentRequest(currentRequestId: String) {
        requestRepository.listenToRequest(currentRequestId) { request ->

            _currentRequestTaken.value = request
            if (request != null) {
                val isStatusFinal = request.status == Status.COMPLETED.displayName ||
                        request.status == Status.CANCELLED.displayName

                val isDataReady = if (request.status == Status.COMPLETED.displayName) {
                    request.rating > 0 && request.points > 0
                } else {
                    true
                }

                if (isStatusFinal && isDataReady) {
                    _showRequestCompletedDialog.value = true
                }
            }
        }
    }

    fun clearCurrentRequest() {
        _currentRequestTaken.value = null
        _showRequestCompletedDialog.value = false
    }

    fun showRequestCompletedDialog(request: Request, lastHandledRequestId: String) {
        val isStatusFinal = request.status == Status.COMPLETED.displayName ||
                request.status == Status.CANCELLED.displayName

        val isDataReady = if (request.status == Status.COMPLETED.displayName) {
            request.rating > 0 && request.points > 0
        } else {
            true
        }

        if (isStatusFinal && isDataReady && lastHandledRequestId != request.requestId) {
            _showRequestCompletedDialog.value = true
        }

    }

    fun createRequest(
        userUid: String,
        userName: String,
        userImage: String,
        title: String,
        description: String,
        location: String,
        selectedLat: Double,
        selectedLng: Double,
        selectedCategory: String,
        urgent: Boolean
    ) {
        val newRequest = Request(
            userId = userUid,
            userName = userName,
            userImageUrl = userImage,
            title = title,
            content = description,
            locationInfo = location,
            latitude = selectedLat,
            longitude = selectedLng,
            urgent = urgent,
            timestamp = System.currentTimeMillis(),
            tag = selectedCategory,
            status = Status.OPEN.displayName
        )
        _isPublishing.value = true
        requestRepository.createRequest(
            newRequest, onSuccess = { requestId ->
                _newRequestId.postValue(requestId)
                _isPublishing.value = false
                _publishSuccess.value = true
            },
            onFailure = {
                _newRequestId.postValue("")
                _isPublishing.value = false
                _publishSuccess.value = false
            })
    }

    fun updateRequestImage(reqId: String, imageUri: Uri) {

        ImageStorage.uploadRequestImage(reqId, imageUri, onSuccess = { imageUrl ->
            requestRepository.updateRequestImage(reqId, imageUrl, onComplete = { _ ->
                _isPublishing.value = false
                _publishSuccess.value = true
            })
        }, onFailure = { _ ->
            _isPublishing.value = false
            _publishSuccess.value = false

        }
        )

    }

    fun fetchNearbyRequests(myUid: String, lat: Double, lng: Double, radiusInMeters: Double) {
        requestRepository.listenToRequestsInRadius(myUid, lat, lng, radiusInMeters) { nearbyList ->
            _requests.postValue(nearbyList)
        }
    }

    fun acceptRequest(requestId: String, helperId: String) {
        requestRepository.acceptRequest(requestId, helperId)
        _selectedRequestHelperId.value = helperId
    }


    fun getUserRequestsOpen(userId: String) {
        requestRepository.getUserRequestsOpen(userId) {
            _openUserRequests.postValue(it)
        }
    }

    fun getUserRequestsArchive(userId: String) {
        requestRepository.getUserRequestsArchive(userId) {
            _userRequests.value = it
        }
    }


    fun completeRequest(requestId: String, completed: Boolean) {
        if (completed)
            requestRepository.completeRequest(requestId)
        else
            requestRepository.cancelRequest(requestId)

    }
}
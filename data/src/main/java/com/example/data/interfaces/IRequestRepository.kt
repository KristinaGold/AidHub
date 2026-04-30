package com.example.data.interfaces

import com.example.data.dataStractures.Request

interface IRequestRepository {

    fun createRequest(
        request: Request,
        onSuccess: (requestId: String) -> Unit,
        onFailure: (Exception) -> Unit
    )
    fun listenToRequest(reqId: String, onSuccess: (Request?) -> Unit)
    fun getRequest(reqId: String, onSuccess: (Request?) -> Unit)
    fun listenToRequestsInRadius(
        myUid: String,
        myLat: Double,
        myLng: Double,
        radiusInMeters: Double,
        callback: (List<Request>) -> Unit
    )
    fun getUserRequestsOpen(userId: String, callback: (List<Request>) -> Unit)
    fun getUserRequestsArchive(userId: String, callback: (List<Request>) -> Unit)
    fun acceptRequest(requestId: String, helperId: String)
    fun completeRequest(requestId: String)
    fun cancelRequest(requestId: String)
    fun rateRequest(requestId: String, rating: Double, points: Int, review: String)
    fun updateRequestImage(reqId: String, imageUrl: String, onComplete: (Boolean) -> Unit)

}
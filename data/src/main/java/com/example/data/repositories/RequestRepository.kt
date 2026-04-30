package com.example.data.repositories

import android.location.Location
import com.example.data.dataStractures.Request
import com.example.data.dataStractures.Status
import com.example.data.interfaces.IRequestRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

internal class RequestRepository private constructor() : IRequestRepository {

    private val db = FirebaseFirestore.getInstance()
    private val requestsCollection = db.collection("Requests")

    companion object {

        @Volatile
        private var instance: RequestRepository? = null

        internal fun init(): RequestRepository {
            return instance ?: synchronized(this) {
                instance ?: RequestRepository().also { instance = it }
            }
        }

        internal fun getInstance(): RequestRepository {
            return instance ?: throw IllegalStateException(
                "RequestRepository must be initialized by calling init() before use."
            )
        }

    }

    override fun createRequest(
        request: Request,
        onSuccess: (requestId: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val requestId = requestsCollection.document().id
        val finalRequest = request.copy(requestId = requestId)

        requestsCollection.document(requestId).set(finalRequest)
            .addOnSuccessListener { onSuccess(requestId) }
            .addOnFailureListener { e -> onFailure(e) }
    }


    override fun listenToRequest(reqId: String, onSuccess: (Request?) -> Unit) {
        requestsCollection.document(reqId).addSnapshotListener { snapshot, e ->

            if (e != null || snapshot == null) return@addSnapshotListener

            val req = snapshot.toObject(Request::class.java)
            onSuccess(req)
        }
    }

    override fun getRequest(reqId: String, onSuccess: (Request?) -> Unit) {
        requestsCollection.document(reqId).get()
            .addOnSuccessListener { document ->
                val req = document.toObject(Request::class.java)
                onSuccess(req)
            }
    }


    override fun listenToRequestsInRadius(
        myUid: String,
        myLat: Double,
        myLng: Double,
        radiusInMeters: Double,
        callback: (List<Request>) -> Unit
    ) {
        val latDelta = radiusInMeters / 111000.0
        // val lngDelta = radiusInMeters / (111000.0 * Math.cos(Math.toRadians(myLat)))

        requestsCollection.whereGreaterThanOrEqualTo("latitude", myLat - latDelta)
            .whereLessThanOrEqualTo("latitude", myLat + latDelta)
            .whereEqualTo("status", Status.OPEN.displayName)
            .whereNotEqualTo("userId", myUid)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val allInLatRange = snapshot.toObjects(Request::class.java)

                val filteredRequests = allInLatRange.filter { req ->
                    val results = FloatArray(1)
                    Location.distanceBetween(myLat, myLng, req.latitude, req.longitude, results)
                    results[0] <= radiusInMeters
                }

                callback(filteredRequests)
            }
    }

    fun getUserRequestsPreview(userId: String, callback: (List<Request>) -> Unit) {
        requestsCollection.whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(3)
            .get()
            .addOnSuccessListener { snapshot ->
                val requests = snapshot.toObjects(Request::class.java)
                callback(requests)
            }
    }


    override fun getUserRequestsOpen(userId: String, callback: (List<Request>) -> Unit) {
        requestsCollection.whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .whereIn("status", listOf(Status.IN_PROGRESS.displayName, Status.OPEN.displayName))
            .get()
            .addOnSuccessListener { snapshot ->
                val requests = snapshot.toObjects(Request::class.java)
                callback(requests)
            }
    }

    override fun getUserRequestsArchive(userId: String, callback: (List<Request>) -> Unit) {
        requestsCollection.whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING).whereIn(
                "status", listOf(Status.COMPLETED.displayName, Status.CANCELLED.displayName)
            )
            .get()
            .addOnSuccessListener { snapshot ->
                val requests = snapshot.toObjects(Request::class.java)
                callback(requests)
            }
    }


    override fun acceptRequest(requestId: String, helperId: String) {
        requestsCollection.document(requestId).update(
            "status", Status.IN_PROGRESS.displayName,
            "helperId", helperId
        )
    }

    override fun completeRequest(requestId: String) {
        requestsCollection.document(requestId).update(
            "status", Status.COMPLETED.displayName
        )
    }

    override fun cancelRequest(requestId: String) {
        requestsCollection.document(requestId).update(
            "status", Status.CANCELLED.displayName, "helperId", ""
        )
    }

    override fun rateRequest(requestId: String, rating: Double, points: Int, review: String) {
        requestsCollection.document(requestId).update(
            "rating", rating,
            "points", points,
            "review", review, "reviewTimestamp", System.currentTimeMillis()
        )
    }

    override fun updateRequestImage(
        reqId: String,
        imageUrl: String,
        onComplete: (Boolean) -> Unit
    ) {
        requestsCollection.document(reqId).update("requestImageUrl", imageUrl)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const {onRequest} = require("firebase-functions/https");
const logger = require("firebase-functions/logger");

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

  
  
const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
admin.initializeApp();


	// 1. Sends notification when a new request opened in my radius and with my relevent skills.

function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

	exports.onNewRequestCreated = functions.firestore
    .document("Requests/{requestId}")
    .onCreate(async (snapshot, context) => {
        const requestData = snapshot.data();
        const requestId = context.params.requestId;
        
        const reqLat = requestData.latitude;
        const reqLon = requestData.longitude;
        const requestTag = requestData.tag;

        if (reqLat === undefined || reqLon === undefined || !requestTag) return null;

        try {
            const usersSnapshot = await admin.firestore().collection("Users")
                .where("skills", "array-contains", requestTag)
                .get();

            const batch = admin.firestore().batch();
            const pushMessages = [];

            usersSnapshot.forEach(doc => {
                const userData = doc.data();
                if (userData.uid !== requestData.userId && userData.lat && userData.lon) {
                    const dist = calculateDistance(reqLat, reqLon, userData.lat, userData.lon);
                    const userMaxDist = userData.maxRadius || 10;

                    if (dist <= userMaxDist) {
                        const notifRef = admin.firestore()
                            .collection("Users").doc(userData.uid)
                            .collection("notifications").doc();
						const notificationId = notifRef.id;
                        
                        batch.set(notifRef, {
							notificationId: notificationId,
                            title: "New help needed!",
                            body: `Someone in range of ${Math.round(dist)} km, needs your help in ${requestTag}`,
                            type: "NEW_REQUEST",
                            relatedId: requestId,
                            timestamp: admin.firestore.FieldValue.serverTimestamp(),
                            read: false
                        });

                        if (userData.fcmToken) {
                            pushMessages.push({
                                token: userData.fcmToken,
                               
								data: {
									title: "New help needed!",
                                    body: `Someone in your area, needs help with ${requestTag}`,
									type: "NEW_REQUEST",
									relatedId: requestId,
									notificationId: notificationId
								},
								android: {
									priority: "high",
								}
                            });
                        }
                    }
                }
            });

            await batch.commit();
            if (pushMessages.length > 0) {
                await admin.messaging().sendEach(pushMessages);
            }
        } catch (error) {
            console.error("Error:", error);
        }
        return null;
    });

	// 2. Sends notification when someone is assigned to help my request (HelperId updated) 

	exports.onRequestHelperUpdated = functions.firestore
    .document("Requests/{requestId}")
    .onUpdate(async (change, context) => {
        const newData = change.after.data();
        const oldData = change.before.data();
        const requestId = context.params.requestId;

        if (!oldData.helperId && newData.helperId) {
            const userId = newData.userId;
            const helperId = newData.helperId;

            try {
                const helperDoc = await admin.firestore().collection("Users").doc(helperId).get();
                const helperName = helperDoc.data().fullName || "Volunteer";


				const notifColRef = admin.firestore().collection("Users").doc(userId).collection("notifications");
                const newNotifRef = notifColRef.doc();
                const notificationId = newNotifRef.id;
				
				
                const notificationData = {
					notificationId: notificationId,
                    title: "Help is on the way!",
                    body: `${helperName} has accepted your ${newData.tag} request`,
                    type: "HELPER_FOUND",
                    relatedId: requestId,
                    timestamp: admin.firestore.FieldValue.serverTimestamp(),
                    read: false
                };
				
				await newNotifRef.set(notificationData);



                const ownerDoc = await admin.firestore().collection("Users").doc(userId).get();
                if (ownerDoc.data().fcmToken) {
                    await admin.messaging().send({
                        token: ownerDoc.data().fcmToken,
                        data: {
							title: notificationData.title,
                            body: notificationData.body,
									type: "HELPER_FOUND",
									relatedId: requestId,
									helperId: helperId,
									notificationId: notificationId
								},
								android: {
									priority: "high",
								}
                    });
                }
            } catch (error) {
                console.error("Error on helper updated:", error);
            }
        }
        return null;
    });

	// 3. Sends push notification when a message recieved (without adding it to the notifications collection

	exports.onChatMessage = functions.firestore
    .document("ChatRooms/{chatRoomId}/messages/{messageId}")
    .onCreate(async (snapshot, context) => {
        const msg = snapshot.data();
        const chatId = context.params.chatRoomId;

        const chatDoc = await admin.firestore().collection("ChatRooms").doc(chatId).get();
        const receiverId = chatDoc.data().participants.find(id => id !== msg.senderId);
		const senderID = msg.senderId;

        const receiverDoc = await admin.firestore().collection("Users").doc(receiverId).get();
        if (receiverDoc.data().fcmToken) {
            await admin.messaging().send({
                token: receiverDoc.data().fcmToken,
                
                data: {
					title: `New message from ${msg.senderName}`,
                    body: msg.text,
									type: "CHAT",
									relatedId: chatId,
									senderId: senderID
								},
								android: {
									priority: "high",
								}
            });
        }
        return null;
    });
	
	// 4. Sends notification when a request currently taken is closed.

	exports.onRequestClosed = functions.firestore
    .document("Requests/{requestId}")
    .onUpdate(async (change, context) => {
        const newData = change.after.data();
        const oldData = change.before.data();
        

        if (newData.status === 'COMPLETED' && oldData.status !== 'COMPLETED') {
            const userId = newData.userId;
            const helperId = newData.helperId;
            const requestId = newData.requestId;
			
			const request = await admin.firestore().collection("Requests").doc(requestId).get();
			const rating = request.data().rating;
            const pointsGained = request.data().points;
            if (!helperId) {
                console.log('No helper associated with this request.');
                return null;
            }

            try {
				
				const notifColRef = admin.firestore().collection("Users").doc(helperId).collection("notifications");
                const newNotifRef = notifColRef.doc();
                const notificationId = newNotifRef.id;
				
				
                const notificationData = {
					notificationId: notificationId,
                    title: "Your request has been completed!",
                    body: `Your rating: ${rating} \nYou scored ${pointsGained} points`,
                    type: "REQUEST_CLOSED",
                    relatedId: requestId,
                    timestamp: admin.firestore.FieldValue.serverTimestamp(),
                    read: false
                };

				await newNotifRef.set(notificationData);


                const ownerDoc = await admin.firestore().collection("Users").doc(helperId).get();
                if (ownerDoc.data().fcmToken) {
                    await admin.messaging().send({
                        token: ownerDoc.data().fcmToken,
                       
                        data: {
							title: notificationData.title,
                            body: notificationData.body,
									type: "REQUEST_CLOSED",
									relatedId: requestId,
									helperId: helperId,
									notificationId: notificationId
								},
								android: {
									priority: "high"
								}
                    });
                }
            } catch (error) {
                console.error("Error on request closed:", error);
            }
        }
        return null;
    });
	
	
	// 5. Sends notification when a post is liked

	exports.onLikeNotification = functions.firestore
    .document("Posts/{postId}")
    .onUpdate(async (change, context) => {
        const newData = change.after.data();
        const oldData = change.before.data();
        const postId = context.params.postId;

        const newLikes = newData.likes || [];
        const oldLikes = oldData.likes || [];

        if (newLikes.length <= oldLikes.length) return null;

        const likerId = newLikes.find(id => !oldLikes.includes(id));

        if (!likerId || likerId === newData.userId) {
            console.log("Like by owner or no new ID found.");
            return null;
        }

        try {
            const likerDoc = await admin.firestore().collection("Users").doc(likerId).get();
            if (!likerDoc.exists) return null;
            
            const likerName = likerDoc.data().fullName || "Someone";
            const postOwnerId = newData.userId;
			
			const notifColRef = admin.firestore().collection("Users").doc(postOwnerId).collection("notifications");
                const newNotifRef = notifColRef.doc();
                const notificationId = newNotifRef.id;

            const notificationData = {
				notificationId: notificationId,
                title: "New Like!",
                body: `${likerName} liked your post`,
                type: "POST_LIKE",
                relatedId: postId,
                timestamp: admin.firestore.FieldValue.serverTimestamp(),
                read: false
            };

				await newNotifRef.set(notificationData);


            const ownerDoc = await admin.firestore().collection("Users").doc(postOwnerId).get();
            if (ownerDoc.exists && ownerDoc.data().fcmToken) {
                await admin.messaging().send({
                    token: ownerDoc.data().fcmToken,
                   
                    android: { priority: "high" },
                    data: {
						title: notificationData.title,
                        body: notificationData.body,
                        type: "POST_LIKE",
                        relatedId: postId,
						notificationId: notificationId
                    }
                });
            }
        } catch (error) {
            console.error("Error sending like notification:", error);
        }
        return null;
    });
	
	
	//6. Updates posts and requests when user data is changed

	exports.onUserUpdate = functions.firestore
    .document('Users/{uid}')
    .onUpdate(async (change, context) => {
        const userId = context.params.uid;
        const newValue = change.after.data();
        const previousValue = change.before.data();

        const nameChanged = newValue.fullName !== previousValue.fullName;
        const photoChanged = newValue.profileImageUrl !== previousValue.profileImageUrl;

        if (!nameChanged && !photoChanged) {
            console.log('No relevant changes in user profile.');
            return null;
        }

        const db = admin.firestore();
        const batch = db.batch();

        const postsSnapshot = await db.collection('Posts')
            .where('userId', '==', userId)
            .get();

        postsSnapshot.forEach(doc => {
            batch.update(doc.ref, {
                userName: newValue.fullName,
                userImageUrl: newValue.profileImageUrl
            });
        });

        const requestsSnapshot = await db.collection('Requests')
            .where('userId', '==', userId)
            .get();

        requestsSnapshot.forEach(doc => {
            batch.update(doc.ref, {
                userName: newValue.fullName,
                userImageUrl: newValue.profileImageUrl
            });
        });

        console.log(`Updating profile for user ${userId} in ${postsSnapshot.size} posts and ${requestsSnapshot.size} requests.`);
        return batch.commit();
    });
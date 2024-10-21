package com.example.businesslinkup.sevices

import android.net.Uri
import android.util.Log
import com.example.businesslinkup.entities.BusinessObject
import com.example.businesslinkup.entities.ObjectType
import com.example.businesslinkup.entities.Comment
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.util.Date


class BusinessObjectRepository(private val firestore: FirebaseFirestore, private val storage: FirebaseStorage) {

    suspend fun addObject(businessObject: BusinessObject, imageUri: Uri?): Result<Unit> {
        return try {
            val photoUrl = imageUri?.let { uploadObjectImage(businessObject.id, it) } ?: ""

            val objectWithImageUrl = businessObject.copy(photoUrl = photoUrl)

            firestore.collection("business_objects").document(objectWithImageUrl.id).set(objectWithImageUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBusinessObjectsInRealTime(): kotlinx.coroutines.flow.Flow<List<BusinessObject>> = kotlinx.coroutines.flow.callbackFlow {
        val listenerRegistration = firestore.collection("business_objects")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val businessObjects = snapshot?.documents?.mapNotNull { document ->
                    println("Document data: ${document.data}")
                    try {
                        val id = document.getString("id") ?: ""
                        val type = document.get("type")?.let { ObjectType.valueOf(it as String) } ?: ObjectType.OTHER
                        val desc = document.getString("desc") ?: ""
                        val title = document.getString("title") ?: ""
                        val latitude = (document.get("latitude") as? Number)?.toDouble() ?: 0.0
                        val longitude = (document.get("longitude") as? Number)?.toDouble() ?: 0.0
                        val address = document.getString("address") ?: ""
                        val createDate = document.getDate("createDate") ?: Date()
                        val userId = document.getString("userId") ?: ""
                        val photoUrl = document.getString("photoUrl") ?: ""

                        BusinessObject(id, type, desc, title, latitude, longitude, address, createDate, userId, photoUrl)
                    } catch (e: Exception) {
                        println("Deserijalizacija greška: ${e.message}")
                        null
                    }
                }.orEmpty()

                trySend(businessObjects).isSuccess
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    fun getBusinessObjectsInRealTimeDistance(userLat: Double, userLon: Double): Flow<List<BusinessObject>> = callbackFlow {
        val listenerRegistration = firestore.collection("business_objects")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val businessObjects = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val id = document.getString("id") ?: ""
                        val type = document.get("type")?.let { ObjectType.valueOf(it as String) } ?: ObjectType.OTHER
                        val desc = document.getString("desc") ?: ""
                        val title = document.getString("title") ?: ""
                        val latitude = (document.get("latitude") as? Number)?.toDouble() ?: 0.0
                        val longitude = (document.get("longitude") as? Number)?.toDouble() ?: 0.0
                        val address = document.getString("address") ?: ""
                        val createDate = document.getDate("createDate") ?: Date()
                        val userId = document.getString("userId") ?: ""
                        val photoUrl = document.getString("photoUrl") ?: ""

                        val distance = GeoLocationHelper.calculateDistance(userLat, userLon, latitude, longitude)
                        if (distance <= 200.0) {
                            BusinessObject(id, type, title, desc, latitude, longitude, address, createDate, userId, photoUrl)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        println("Deserialization error: ${e.message}")
                        null
                    }
                }.orEmpty()

                trySend(businessObjects).isSuccess
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    fun getBusinessObjectsInRealTimeFilterByTypes(
        types: Set<String>,
        d: Double = Double.MAX_VALUE,
        userLat: Double,
        userLon: Double,
        tlt: String? = ""
    ): Flow<List<BusinessObject>> = callbackFlow {
        val listenerRegistration = firestore.collection("business_objects")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val businessObjects = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val id = document.getString("id") ?: ""
                        val type = (document.get("type") as? String)?.let { ObjectType.valueOf(it) } ?: ObjectType.OTHER
                        val desc = document.getString("desc") ?: ""
                        val title = document.getString("title") ?: ""
                        val latitude = (document.get("latitude") as? Number)?.toDouble() ?: 0.0
                        val longitude = (document.get("longitude") as? Number)?.toDouble() ?: 0.0
                        val address = document.getString("address") ?: ""
                        val createDate = document.getDate("createDate") ?: Date()
                        val userId = document.getString("userId") ?: ""
                        val photoUrl = document.getString("photoUrl") ?: ""

                        val distance = GeoLocationHelper.calculateDistance(userLat, userLon, latitude, longitude)

                        if (distance <= d &&
                            (tlt.isNullOrBlank() || title.contains(tlt, ignoreCase = true)) &&
                            (types.isEmpty() || types.contains(type.name))
                        ) {
                            BusinessObject(id, type, title, desc, latitude, longitude, address, createDate, userId, photoUrl)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        println("Deserialization error: ${e.message}")
                        null
                    }
                }.orEmpty()

                trySend(businessObjects).isSuccess
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }



    private suspend fun uploadObjectImage(uid: String, imageUri: Uri): String {
        return try {
            val uploadTask = storage.reference.child("business_objects/$uid.jpg").putFile(imageUri)
            val downloadUrlTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                    throw Exception("Image upload failed")
                }
                storage.reference.child("business_objects/$uid.jpg").downloadUrl
            }
            val downloadUrl = downloadUrlTask.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Image upload failed", e)
        }
    }

    fun getBusinessObjectById(id: String): Flow<BusinessObject?> = flow {
        try {
            val document = firestore.collection("business_objects").document(id).get().await()
            val businessObject = document.toBusinessObject()
            emit(businessObject)
            Log.d("BusinessViewModel", "Desc: ${businessObject!!.desc ?: "No description"}")
        } catch (e: Exception) {
            println("Error fetching document: ${e.message}")
            emit(null)
        }
    }

    private fun DocumentSnapshot.toBusinessObject(): BusinessObject? {
        return try {
            val id = getString("id") ?: ""
            val type = (get("type") as? String)?.let { ObjectType.valueOf(it) } ?: ObjectType.OTHER
            val desc = getString("desc") ?: ""
            val title = getString("title") ?: ""
            val latitude = (get("latitude") as? Number)?.toDouble() ?: 0.0
            val longitude = (get("longitude") as? Number)?.toDouble() ?: 0.0
            val address = getString("address") ?: ""
            val createDate = getDate("createDate") ?: Date()
            val userId = getString("userId") ?: ""
            val photoUrl = getString("photoUrl") ?: ""

            BusinessObject(id, type, title, desc, latitude, longitude, address, createDate, userId, photoUrl)
        } catch (e: Exception) {
            // Logovanje greške u pretvaranju
            println("Deserialization error: ${e.message}")
            null
        }
    }

    suspend fun addComment(comment: Comment): Result<Unit> {
        return try {
            firestore.collection("comments").document(comment.id).set(comment).await()
            updateUserPoints(comment.targetUserId, 10)
            updateUserPoints(comment.authorId, 1)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCommentsByObjectId(objectId: String): Flow<List<Comment>> = callbackFlow {
        val listenerRegistration = firestore.collection("comments")
            .whereEqualTo("objectId", objectId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val comments = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Comment::class.java)
                }.orEmpty()

                trySend(comments).isSuccess
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    private suspend fun updateUserPoints(userId: String, pointsToAdd: Int) {
        try {
            val userRef = firestore.collection("users").document(userId)
            firestore.runTransaction { transaction ->
                val userDocument = transaction.get(userRef)
                val currentPoints = userDocument.getLong("points")?.toInt() ?: 0
                transaction.update(userRef, "points", currentPoints + pointsToAdd)
            }.await()
        } catch (e: Exception) {
            throw Exception("Failed to update user points", e)
        }
    }
}
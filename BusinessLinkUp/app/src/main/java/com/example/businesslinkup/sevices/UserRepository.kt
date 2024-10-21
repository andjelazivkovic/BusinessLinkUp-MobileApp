package com.example.businesslinkup.sevices

import android.net.Uri
import android.util.Log
import com.example.businesslinkup.entities.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {


    suspend fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String,
        imageUri: Uri?
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User ID is null")

            val profileImageUrl = imageUri?.let {
                uploadProfileImage(uid, it)
            } ?: ""

            val user = User(
                id = uid,
                username = username,
                email = email,
                firstName = firstName,
                lastName = lastName,
                profilePictureUrl = profileImageUrl,
                rank=0,
                points=0,
                password="",
                latitude=0.0,
                longitude = 0.0
            )
            saveUserToFirestore(user).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User ID is null")
            val user = getUserFromFirestore(uid)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadProfileImage(uid: String, imageUri: Uri): String {
        return try {
            val uploadTask = storage.reference.child("profile_images/$uid.jpg").putFile(imageUri)
            val downloadUrlTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                    throw Exception("Image upload failed")
                }
                storage.reference.child("profile_images/$uid.jpg").downloadUrl
            }
            val downloadUrl = downloadUrlTask.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Image upload failed", e)
        }
    }

    suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }


    private fun saveUserToFirestore(user: User) =
        firestore.collection("users").document(user.id).set(user)

    suspend fun getUserFromFirestore(uid: String): User {
        val document = firestore.collection("users").document(uid).get().await()
        return if (document.exists()) {
            val data = document.data
            User(
                id = data?.get("id") as? String ?: throw Exception("Invalid ID"),
                username = data?.get("username") as? String ?: throw Exception("Invalid Username"),
                email = data?.get("email") as? String ?: throw Exception("Invalid Email"),
                firstName = data?.get("firstName") as? String ?: throw Exception("Invalid First Name"),
                lastName = data?.get("lastName") as? String ?: throw Exception("Invalid Last Name"),
                profilePictureUrl = data?.get("profilePictureUrl") as? String ?: "",
                points = (data?.get("points") as? Long)?.toInt() ?: 0,
                rank = (data?.get("rank") as? Long)?.toInt() ?: 0,
                latitude = (data?.get("latitude") as? Double) ?: 0.0,
                longitude = (data?.get("longitude") as? Double) ?: 0.0
            )
        } else {
            throw Exception("User not found")
        }
    }

    suspend fun updateUser(
        uid: String,
        firstName: String,
        lastName: String,
    ): Result<User> {
        return try {

            val user = firestore.collection("users").document(uid).get().await().toObject(User::class.java)!!
            val updatedUser = user.copy(
                firstName = firstName,
                lastName = lastName,
            )
            saveUserToFirestore(updatedUser).await()

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            user.reauthenticate(credential).await()

            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_WRONG_PASSWORD" -> Result.failure(Exception("The current password is incorrect"))
                else -> Result.failure(Exception("Password change failed: ${e.localizedMessage}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Password change failed: ${e.localizedMessage}"))
        }
    }

    suspend fun changeProfilePicture(uid: String, profileImageUri: Uri): Result<String> {
        return try {
            val profileImageUrl = uploadProfileImage(uid, profileImageUri)
            firestore.collection("users").document(uid).update("profilePictureUrl", profileImageUrl).await()
            Result.success(profileImageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.documents.map { document ->
                document.toObject(User::class.java)!!
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}

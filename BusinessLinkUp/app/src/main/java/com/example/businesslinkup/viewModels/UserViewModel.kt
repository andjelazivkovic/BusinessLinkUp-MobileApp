package com.example.businesslinkup.viewModels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesslinkup.sevices.UserRepository
import com.example.businesslinkup.entities.User
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _userState = MutableStateFlow<Result<User>?>(null)
    val userState: StateFlow<Result<User>?> = _userState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _currentuser = MutableStateFlow<User?>(null)
    val current: StateFlow<User?> = _currentuser

    private val _changePasswordState = MutableStateFlow<Result<Unit>?>(null)
    val changePasswordState: StateFlow<Result<Unit>?> = _changePasswordState

    private val _changePasswordError = MutableStateFlow<String?>(null)
    val changePasswordError: StateFlow<String?> = _changePasswordError

    private val _signOutError = MutableStateFlow<String?>(null)
    val signOutError: StateFlow<String?> = _signOutError

    private val _usersState = MutableStateFlow<List<User>?>(null)
    val usersState: StateFlow<List<User>?> = _usersState


    fun getUserById(uid: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserFromFirestore(uid)
                _user.value = user
            } catch (e: Exception) {

                _user.value = null
            }
        }
    }

    fun registerUser(email: String, password: String, firstname: String, lastname: String, username: String, imageUri: Uri?) {
        viewModelScope.launch {
            val result = userRepository.registerUser(email, password, firstname, lastname, username, imageUri)
            _userState.value = result
        }
    }

    suspend fun getIdCurrentUser():String? {
        val result=userRepository.getCurrentUserId()
        return result
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            try {
                val uid = userRepository.getCurrentUserId()
                if (uid != null) {
                    val user = userRepository.getUserFromFirestore(uid)
                    _currentuser.value = user
                } else {
                    _currentuser.value = null
                }
            } catch (e: Exception) {
                _currentuser.value = null
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.loginUser(email, password)
            _userState.value = result
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val users = userRepository.getAllUsers()
                _usersState.value = users.sortedByDescending { it.points }
            } catch (e: Exception) {
                _usersState.value = null
            }
        }
    }

    fun updateUser(
        firstName: String,
        lastName: String,
    ) {
        viewModelScope.launch {
            val uid = userRepository.getCurrentUserId()
            if (uid != null) {
                val result = userRepository.updateUser(uid, firstName, lastName)
                _userState.value = result
                if (result.isSuccess) {
                    _currentuser.value = result.getOrNull()
                }
            }
        }
    }


    fun changePassword(currentPassword: String, newPassword: String, callback: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.changePassword(currentPassword, newPassword)
            if (result.isSuccess) {
                _changePasswordError.value = null
                callback(result)
            } else {
                _changePasswordError.value = result.exceptionOrNull()?.localizedMessage ?: "Password change failed"
                callback(result)
            }
        }
    }



    fun changeProfilePicture(profileImageUri: Uri) {
        viewModelScope.launch {
            val uid = userRepository.getCurrentUserId()
            if (uid != null) {
                val result = userRepository.changeProfilePicture(uid, profileImageUri)
                if (result.isSuccess) {
                    _currentuser.value = _currentuser.value?.copy(profilePictureUrl = result.getOrNull() ?: "")
                } else {
                    _changePasswordError.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update profile picture"
                }
            }
        }
    }


    fun signOut(callback: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.signOut()
            if (result.isSuccess) {
                _currentuser.value = null
                _signOutError.value = null
                callback(result)
            } else {
                _signOutError.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to sign out"
                callback(result)
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return userRepository.isUserLoggedIn()
    }
}

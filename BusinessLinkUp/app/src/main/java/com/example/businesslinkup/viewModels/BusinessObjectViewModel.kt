package com.example.businesslinkup.viewModels

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.businesslinkup.sevices.GeocodingApi
import com.example.businesslinkup.sevices.BusinessObjectRepository
import com.example.businesslinkup.sevices.UserRepository
import com.example.businesslinkup.entities.BusinessObject
import com.example.businesslinkup.entities.Comment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesslinkup.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BusinessObjectViewModel(
    private val objectRepository: BusinessObjectRepository, private val userRepository: UserRepository
) : ViewModel() {

    private val _objectState = MutableStateFlow<Result<Unit>?>(null)
    val objectState: StateFlow<Result<Unit>?> = _objectState

    private val _businessObjects = MutableStateFlow<List<BusinessObject>>(emptyList())
    val businessObjects: StateFlow<List<BusinessObject>> = _businessObjects

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _businessObject = MutableStateFlow<BusinessObject?>(null)
    val businessObject: StateFlow<BusinessObject?> = _businessObject

    private val _commentAuthors = MutableStateFlow<Map<String, User>>(emptyMap())
    val commentAuthors: StateFlow<Map<String, User>> = _commentAuthors

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation

    private val _businessObjectUser = MutableStateFlow<User?>(null)
    val businessObjectUser: StateFlow<User?> = _businessObjectUser

    fun setUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = Pair(latitude, longitude)
    }

    fun addObject(businessObject: BusinessObject, imageUri: Uri?) {
        viewModelScope.launch {
            val result = objectRepository.addObject(businessObject, imageUri)
            _objectState.value = result
        }
    }

    fun fetchBusinessObjectsInRealTime() {
        viewModelScope.launch {
            objectRepository.getBusinessObjectsInRealTime().collect { objects ->
                _businessObjects.value = objects
            }
        }
    }

    fun searchBusinessObjects(
        title: String,
        types: Set<String>,
        maxDistance: Double,
        userLat: Double,
        userLon: Double
    ) {
        viewModelScope.launch {
            objectRepository.getBusinessObjectsInRealTimeFilterByTypes(
                types = types,
                d = maxDistance,
                userLat = userLat,
                userLon = userLon,
                tlt = title
            ).collect { objects ->
                _businessObjects.value = objects
            }
        }
    }

    fun fetchBusinessObjectsInRealTimeDistance(userLat: Double, userLon: Double) {
        viewModelScope.launch {
            objectRepository.getBusinessObjectsInRealTimeDistance(userLat, userLon).collect { objects ->
                _businessObjects.value = objects
            }
        }
    }

    fun getBusinessObjectById(id: String): Flow<BusinessObject?> {
        return objectRepository.getBusinessObjectById(id)
    }

    fun addComment(comment: Comment) {
        viewModelScope.launch {
            val result = objectRepository.addComment(comment)
            if (result.isSuccess) {
                println("Comment added successfully")
            } else {
                println("Failed to add comment: ${result.exceptionOrNull()?.message}")
            }
        }
    }


    fun loadBusinessObjectAndComments(objectId: String) {
        viewModelScope.launch {
            _comments.value = emptyList()
            _businessObject.value = null

            objectRepository.getBusinessObjectById(objectId).collect { obj ->
                _businessObject.value = obj

                obj?.let { businessObject ->
                    val user = userRepository.getUserFromFirestore(businessObject.userId)
                    _businessObjectUser.value = user
                }
            }


            objectRepository.getCommentsByObjectId(objectId).collect { commentList ->
                _comments.value = commentList


                val authors = mutableMapOf<String, User>()
                commentList.forEach { comment ->
                    userRepository.getUserFromFirestore(comment.authorId)?.let { user ->
                        authors[comment.authorId] = user
                    }
                }
                _commentAuthors.value = authors
            }
        }
    }

    fun getCommentsByObjectId(objectId: String) {
        viewModelScope.launch {

            _comments.value = emptyList()


            objectRepository.getCommentsByObjectId(objectId).collect { commentList ->
                _comments.value = commentList.sortedBy { it.timestamp }
            }
        }
    }
}

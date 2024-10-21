package com.example.businesslinkup.entities

data class User(
    val id: String = "",
    val username: String = "",
    val email: String="",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profilePictureUrl: String = "",
    val points: Int = 0,
    val rank: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

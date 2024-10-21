package com.example.businesslinkup.entities

import com.google.firebase.Timestamp
import java.util.Date

data class Comment(
    val id: String = "",
    val objectId: String = "",
    val authorId: String = "",
    val targetUserId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

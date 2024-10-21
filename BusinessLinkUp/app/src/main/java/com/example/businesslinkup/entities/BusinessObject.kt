package com.example.businesslinkup.entities

import java.util.Date

data class BusinessObject(
    val id: String = "",
    val type: ObjectType = ObjectType.OTHER,
    val title: String="",
    val desc: String = "",
    val latitude: Double=0.0,
    val longitude: Double=0.0,
    val address: String="",
    val createDate: Date = Date(),
    val userId: String = "",
    val photoUrl: String = ""
)

enum class ObjectType {
    BUSINESS_EVENT,
    COWORKING_SPACE,
    WORKSPACE,
    INVESTMENT,
    OTHER
}


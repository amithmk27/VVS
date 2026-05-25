package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val mobileNumber: String,
    val password: String? = null,
    val gender: String, // "Male" or "Female"
    val dob: String, // DOB e.g. "1995-04-12"
    val community: String, // "Madhwa", "Smartha", "Iyengar", "Iyer", "Havyaka", "Shivalli", "Kota", "Others"
    val subSect: String, // e.g., "Vadama", "Badaganadu", "Mulukanadu"
    val education: String, // e.g., "M.Tech", "MBA", "B.E."
    val occupation: String, // e.g., "Software Engineer", "Bank Manager"
    val location: String, // e.g., "Bengaluru", "Chennai"
    val height: String, // e.g., "5'8\"", "5'4\""
    val maritalStatus: String, // "Never Married", "Divorced", "Widowed"
    val aboutMe: String, // Quick summary bio
    val avatarUrl: String, // Profile photo (Preset drawable path or local base64/URI)
    val additionalPhotosJson: String = "[]", // Serialized string array
    val isCurrentUser: Boolean = false,
    val isShortlisted: Boolean = false,
    val registeredTimestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "match_requests")
data class MatchRequest(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val status: String, // "PENDING", "ACCEPTED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class ChatSession(
    val recipientId: String,
    val recipientName: String,
    val recipientAvatarUrl: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0
)

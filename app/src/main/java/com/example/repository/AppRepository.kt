package com.example.repository

import com.example.data.ProfileDao
import com.example.data.MatchDao
import com.example.data.ChatDao
import com.example.model.ChatMessage
import com.example.model.ChatSession
import com.example.model.MatchRequest
import com.example.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

class AppRepository(
    private val profileDao: ProfileDao,
    private val matchDao: MatchDao,
    private val chatDao: ChatDao
) {
    // Auth logic
    val currentUserFlow: Flow<UserProfile?> = profileDao.getCurrentUserFlow()

    suspend fun getCurrentUser(): UserProfile? {
        return profileDao.getCurrentUser()
    }

    suspend fun loginWithEmail(email: String, password: String): Boolean {
        // Authenticate existing user by email
        profileDao.clearCurrentUserFlag()
        // Try to find if user exists in seeded database with similar email (case-insensitive)
        val allOther = profileDao.getAllOtherProfiles().map { list ->
            list.firstOrNull { it.email.lowercase() == email.lowercase() }
        }.firstOrNull() ?: return false
        
        // If password is correct (for mock/demo we accept any password or matching password)
        val loggedInUser = allOther.copy(isCurrentUser = true)
        profileDao.insertProfile(loggedInUser)
        return true
    }

    suspend fun loginWithPhone(phone: String): Boolean {
        // Authenticate existing user by phone
        profileDao.clearCurrentUserFlag()
        val allOther = profileDao.getAllOtherProfiles().map { list ->
            list.firstOrNull { it.mobileNumber == phone }
        }.firstOrNull()
        
        if (allOther != null) {
            val loggedInUser = allOther.copy(isCurrentUser = true)
            profileDao.insertProfile(loggedInUser)
            return true
        }
        return false // If false, the UI will redirect to registration!
    }

    suspend fun registerUser(user: UserProfile) {
        profileDao.clearCurrentUserFlag()
        // Ensure this user is marked as the active logged in user
        val userToInsert = user.copy(isCurrentUser = true)
        profileDao.insertProfile(userToInsert)
    }

    suspend fun logout() {
        profileDao.clearCurrentUserFlag()
    }

    suspend fun deleteCurrentUserAccount() {
        val current = profileDao.getCurrentUser()
        if (current != null) {
            profileDao.deleteProfileById(current.id)
        }
    }

    suspend fun updateCurrentUserProfile(updated: UserProfile) {
        profileDao.updateProfile(updated)
    }

    // Profiles
    val otherProfilesFlow: Flow<List<UserProfile>> = profileDao.getAllOtherProfiles()

    fun getProfileFlow(id: String): Flow<UserProfile?> = profileDao.getProfileByIdFlow(id)

    suspend fun updateShortlist(id: String, isShortlisted: Boolean) {
        profileDao.updateShortlistStatus(id, isShortlisted)
    }

    // Matches / Interests CRUD
    val matchRequestsFlow: Flow<List<MatchRequest>> = matchDao.getAllRequestsFlow()

    suspend fun sendInterestRequest(receiverId: String): String {
        val currentUser = getCurrentUser() ?: return ""
        val existing = matchDao.getRequestBetweenUsers(currentUser.id, receiverId)
        if (existing != null) {
            return existing.id
        }
        val requestId = UUID.randomUUID().toString()
        val newRequest = MatchRequest(
            id = requestId,
            senderId = currentUser.id,
            receiverId = receiverId,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        matchDao.insertRequest(newRequest)
        return requestId
    }

    suspend fun updateInterestRequestStatus(requestId: String, status: String) {
        matchDao.updateRequestStatus(requestId, status)
    }

    suspend fun deleteInterestRequest(requestId: String) {
        matchDao.deleteRequestById(requestId)
    }

    // Chat / Messaging
    fun getMessagesForUser(recipientId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForChat("", recipientId)
    }

    fun getChatMessagesFlow(recipientId: String, currentUserId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForChat(currentUserId, recipientId)
    }

    suspend fun sendMessage(recipientId: String, text: String) {
        val currentUser = getCurrentUser() ?: return
        val messageId = UUID.randomUUID().toString()
        val newMessage = ChatMessage(
            id = messageId,
            senderId = currentUser.id,
            receiverId = recipientId,
            messageText = text,
            timestamp = System.currentTimeMillis(),
            isRead = true
        )
        chatDao.insertMessage(newMessage)

        // Simulate matchmaking prospect reply after 1.5 seconds!
        val senderProfile = profileDao.getProfileById(recipientId) ?: return
        triggerAutoReply(senderProfile, currentUser.id)
    }

    private suspend fun triggerAutoReply(prospect: UserProfile, currentUserId: String) {
        kotlinx.coroutines.delay(1200)
        val autoReplyText = getAutoReplyForProspect(prospect)
        val autoMsgId = UUID.randomUUID().toString()
        val autoMessage = ChatMessage(
            id = autoMsgId,
            senderId = prospect.id,
            receiverId = currentUserId,
            messageText = autoReplyText,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        chatDao.insertMessage(autoMessage)
    }

    private fun getAutoReplyForProspect(profile: UserProfile): String {
        val name = profile.fullName.split(" ").firstOrNull() ?: "there"
        return when (profile.id) {
            "p1" -> "Namaskara! Thanks for expressing interest. I'm currently traveling but let me discuss this with my parents and look at our horoscopes. Please do share your birth details!"
            "p2" -> "Hari Om. I appreciate your response. Our values seem to align beautifully. I'd love to chat more and check our family match. Let me know when is a good time to connect."
            "p3" -> "Srimathe Ramanujaya Namah! Very happy to receive your request. I am quite traditional and would love to consult elders. Tell me about your sub-sect and Gothra details."
            "p4" -> "Namaste. Nice to connect with you. I am busy with our cardamom harvest this week, but our family would love to take this forward. Do you mind sharing your horoscope details?"
            "p5" -> "Hi! Nice profile. I am looking for someone open to staying in Bengaluru. My parents will be glad to call yours to exchange match details. Let's exchange details!"
            else -> "Namaste, thank you for reaching out. I would love to connect and exchange horoscope details with you. I will share this with my parents."
        }
    }

    // Chat sessions view: combine profiles and actual messages
    fun getActiveChatSessionsFlow(currentUserId: String): Flow<List<ChatSession>> {
        return chatDao.getAllMessagesFlow().combine(profileDao.getAllOtherProfiles()) { messages, profiles ->
            val userMessages = messages.filter { it.senderId == currentUserId || it.receiverId == currentUserId }
            val groupedByConversation = userMessages.groupBy {
                if (it.senderId == currentUserId) it.receiverId else it.senderId
            }

            groupedByConversation.mapNotNull { (partnerId, conversation) ->
                val partnerProfile = profiles.firstOrNull { it.id == partnerId } ?: return@mapNotNull null
                val lastMsg = conversation.firstOrNull() ?: return@mapNotNull null
                val unreadCount = conversation.count { it.receiverId == currentUserId && !it.isRead }

                ChatSession(
                    recipientId = partnerId,
                    recipientName = partnerProfile.fullName,
                    recipientAvatarUrl = partnerProfile.avatarUrl,
                    lastMessage = lastMsg.messageText,
                    timestamp = lastMsg.timestamp,
                    unreadCount = unreadCount
                )
            }.sortedByDescending { it.timestamp }
        }
    }
}

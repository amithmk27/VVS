package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.MatchRequest
import com.example.model.UserProfile
import com.example.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MatchViewModel(private val repository: AppRepository) : ViewModel() {

    private val currentUser = repository.currentUserFlow
    private val otherProfiles = repository.otherProfilesFlow
    private val matchRequests = repository.matchRequestsFlow

    // Track active requests sent by me
    val sentInterests: StateFlow<List<Pair<MatchRequest, UserProfile>>> = combine(
        currentUser,
        otherProfiles,
        matchRequests
    ) { curr, profiles, requests ->
        if (curr == null) return@combine emptyList()
        requests.filter { it.senderId == curr.id }
            .mapNotNull { req ->
                val profile = profiles.firstOrNull { it.id == req.receiverId } ?: return@mapNotNull null
                req to profile
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Track active interests received from others
    val receivedInterests: StateFlow<List<Pair<MatchRequest, UserProfile>>> = combine(
        currentUser,
        otherProfiles,
        matchRequests
    ) { curr, profiles, requests ->
        if (curr == null) return@combine emptyList()
        requests.filter { it.receiverId == curr.id && it.status == "PENDING" }
            .mapNotNull { req ->
                val profile = profiles.firstOrNull { it.id == req.senderId } ?: return@mapNotNull null
                req to profile
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Track accepted match requests
    val mutualMatches: StateFlow<List<UserProfile>> = combine(
        currentUser,
        otherProfiles,
        matchRequests
    ) { curr, profiles, requests ->
        if (curr == null) return@combine emptyList()
        val acceptedPartnerIds = requests.filter { 
            (it.senderId == curr.id || it.receiverId == curr.id) && it.status == "ACCEPTED" 
        }.map { 
            if (it.senderId == curr.id) it.receiverId else it.senderId 
        }.toSet()

        profiles.filter { acceptedPartnerIds.contains(it.id) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Send Interest call
    fun sendInterest(receiverId: String) {
        viewModelScope.launch {
            repository.sendInterestRequest(receiverId)
        }
    }

    // Accept interest
    fun acceptInterest(requestId: String) {
        viewModelScope.launch {
            repository.updateInterestRequestStatus(requestId, "ACCEPTED")
        }
    }

    // Reject interest
    fun rejectInterest(requestId: String) {
        viewModelScope.launch {
            repository.updateInterestRequestStatus(requestId, "REJECTED")
        }
    }

    // Cancel interest
    fun cancelInterest(requestId: String) {
        viewModelScope.launch {
            repository.deleteInterestRequest(requestId)
        }
    }

    // Check matchmaking status helper
    fun getMatchStatusWith(receiverId: String): Flow<String?> = combine(
        currentUser,
        matchRequests
    ) { curr, requests ->
        if (curr == null) return@combine null
        val match = requests.firstOrNull { 
            (it.senderId == curr.id && it.receiverId == receiverId) || 
            (it.senderId == receiverId && it.receiverId == curr.id)
        } ?: return@combine null
        
        if (match.senderId == curr.id) {
            "SENT_${match.status}"
        } else {
            "RECEIVED_${match.status}"
        }
    }
}

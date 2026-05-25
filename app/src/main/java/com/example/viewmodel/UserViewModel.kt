package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.UserProfile
import com.example.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray

data class SearchFilters(
    val query: String,
    val gender: String?,
    val community: String?,
    val location: String?,
    val currentUser: UserProfile?
)

class UserViewModel(private val repository: AppRepository) : ViewModel() {

    // Filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _genderFilter = MutableStateFlow<String?>("Female") // default view for demo
    val genderFilter = _genderFilter.asStateFlow()

    private val _communityFilter = MutableStateFlow<String?>(null)
    val communityFilter = _communityFilter.asStateFlow()

    private val _locationFilter = MutableStateFlow<String?>(null)
    val locationFilter = _locationFilter.asStateFlow()

    // Combined filtered profiles stream
    private val filtersFlow: Flow<SearchFilters> = combine(
        _searchQuery,
        _genderFilter,
        _communityFilter,
        _locationFilter,
        repository.currentUserFlow
    ) { query, gender, community, location, currentUser ->
        SearchFilters(query, gender, community, location, currentUser)
    }

    val profilesState: StateFlow<List<UserProfile>> = repository.otherProfilesFlow.combine(filtersFlow) { profiles, filters ->
        val query = filters.query
        val gender = filters.gender
        val community = filters.community
        val location = filters.location
        val currentUser = filters.currentUser
        
        // Auto-configure opposite gender as search default view
        val preferredGender = if (currentUser != null) {
            if (currentUser.gender == "Male") "Female" else "Male"
        } else {
            gender
        }

        profiles.filter { profile ->
            val matchesQuery = query.isEmpty() || 
                    profile.fullName.contains(query, ignoreCase = true) ||
                    profile.occupation.contains(query, ignoreCase = true) ||
                    profile.education.contains(query, ignoreCase = true)
            
            val matchesGender = preferredGender == null || profile.gender.equals(preferredGender, ignoreCase = true)
            val matchesCommunity = community == null || profile.community.equals(community, ignoreCase = true)
            val matchesLocation = location == null || profile.location.contains(location, ignoreCase = true)

            matchesQuery && matchesGender && matchesCommunity && matchesLocation
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Shortlisted items matching
    val shortlistedProfiles: StateFlow<List<UserProfile>> = repository.otherProfilesFlow.map { list ->
        list.filter { it.isShortlisted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGenderFilter(gender: String?) {
        _genderFilter.value = gender
    }

    fun setCommunityFilter(community: String?) {
        _communityFilter.value = community
    }

    fun setLocationFilter(location: String?) {
        _locationFilter.value = location
    }

    fun toggleShortlist(id: String, isShortlisted: Boolean) {
        viewModelScope.launch {
            repository.updateShortlist(id, isShortlisted)
        }
    }

    // Save profile details changes
    fun updateSelfProfile(updatedProfile: UserProfile) {
        viewModelScope.launch {
            repository.updateCurrentUserProfile(updatedProfile)
        }
    }

    // Simulated cloud storage uploads (Profile Picture and multiple album photo changes)
    fun uploadProfileImage(avatarToken: String) {
        viewModelScope.launch {
            val current = repository.getCurrentUser() ?: return@launch
            // Update profile with new primary avatar
            val updated = current.copy(avatarUrl = avatarToken)
            repository.updateCurrentUserProfile(updated)
        }
    }

    fun addAlbumPhoto(photoToken: String) {
        viewModelScope.launch {
            val current = repository.getCurrentUser() ?: return@launch
            val arr = try {
                val list = ArrayList<String>()
                val json = JSONArray(current.additionalPhotosJson)
                for (i in 0 until json.length()) {
                    list.add(json.getString(i))
                }
                list
            } catch (e: Exception) {
                arrayListOf(current.avatarUrl)
            }

            if (!arr.contains(photoToken)) {
                arr.add(photoToken)
            }

            val updatedJson = JSONArray(arr).toString()
            val updated = current.copy(additionalPhotosJson = updatedJson)
            repository.updateCurrentUserProfile(updated)
        }
    }

    fun deleteAlbumPhoto(photoToken: String) {
        viewModelScope.launch {
            val current = repository.getCurrentUser() ?: return@launch
            val arr = try {
                val list = ArrayList<String>()
                val json = JSONArray(current.additionalPhotosJson)
                for (i in 0 until json.length()) {
                    list.add(json.getString(i))
                }
                list
            } catch (e: Exception) {
                ArrayList<String>()
            }

            arr.remove(photoToken)
            val updatedJson = JSONArray(arr).toString()
            
            // If primary avatar was deleted, fall back to another image or placeholder
            val newAvatar = if (current.avatarUrl == photoToken) {
                arr.firstOrNull() ?: "avatar_placeholder"
            } else {
                current.avatarUrl
            }

            val updated = current.copy(
                avatarUrl = newAvatar,
                additionalPhotosJson = updatedJson
            )
            repository.updateCurrentUserProfile(updated)
        }
    }

    fun parseAlbumPhotos(jsonStr: String): List<String> {
        return try {
            val list = ArrayList<String>()
            val json = JSONArray(jsonStr)
            for (i in 0 until json.length()) {
                list.add(json.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getProfileFlow(id: String): Flow<UserProfile?> {
        return repository.getProfileFlow(id)
    }
}

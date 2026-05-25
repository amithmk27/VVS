package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.UserProfile
import com.example.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(private val repository: AppRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    private val _currentUser = repository.currentUserFlow
    val currentUser = _currentUser

    init {
        // Sync SQLite session with active FirebaseAuth session on startup
        viewModelScope.launch {
            try {
                val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val currentFbUser = firebaseAuth.currentUser
                if (currentFbUser != null) {
                    val localUser = repository.getCurrentUser()
                    if (localUser == null) {
                        // User is authenticated in Firebase, attempt local sync using their phone number or email
                        val phoneNumber = currentFbUser.phoneNumber
                        if (!phoneNumber.isNullOrEmpty()) {
                            val cleanNumber = phoneNumber.takeLast(10)
                            val signedIn = repository.loginWithPhone(cleanNumber)
                            if (signedIn) {
                                _authState.value = AuthState.Authenticated
                            }
                        } else {
                            val email = currentFbUser.email
                            if (!email.isNullOrEmpty()) {
                                val signedIn = repository.loginWithEmail(email, "")
                                if (signedIn) {
                                    _authState.value = AuthState.Authenticated
                                }
                            }
                        }
                    } else {
                        _authState.value = AuthState.Authenticated
                    }
                }
            } catch (e: Exception) {
                // Ignore if not initialized
            }
        }
    }

    // Mobile Phone Authentication flows using integrated Firebase Auth Service
    fun sendOtp(activity: android.app.Activity, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            // Format phone number
            val digits = phoneNumber.filter { it.isDigit() }
            val cleanPhone = when {
                digits.startsWith("91") && digits.length > 10 -> digits.substring(2).take(10)
                digits.startsWith("0") && digits.length > 10 -> digits.substring(1).take(10)
                else -> digits.take(10)
            }
            
            if (cleanPhone.length < 10) {
                _authState.value = AuthState.Error("Invalid mobile number. Please enter a valid 10-digit number.")
                return@launch
            }

            com.example.data.AuthService.verifyPhoneNumber(
                activity = activity,
                phoneNumber = cleanPhone,
                onVerificationCompleted = { credential ->
                    // Automated verification (e.g. instant SMS detection on target devices)
                    viewModelScope.launch {
                        _authState.value = AuthState.Loading
                        try {
                            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    viewModelScope.launch {
                                        val signedIn = repository.loginWithPhone(cleanPhone)
                                        if (signedIn) {
                                            _authState.value = AuthState.Authenticated
                                        } else {
                                            _authState.value = AuthState.RegisterRequired(cleanPhone)
                                        }
                                    }
                                } else {
                                    val errMsg = task.exception?.message ?: "Instant verification failed."
                                    val displayMsg = if (errMsg.contains("API key not valid", ignoreCase = true)) {
                                        "Firebase API Key is invalid. Please configure your custom Firebase Credentials in the Secrets panel."
                                    } else {
                                        errMsg
                                    }
                                    _authState.value = AuthState.Error(displayMsg)
                                }
                            }
                        } catch (e: Exception) {
                            _authState.value = AuthState.Error(e.message ?: "Instant verification error.")
                        }
                    }
                },
                onVerificationFailed = { exception ->
                    val errMsg = exception.message ?: "Verification failed."
                    val displayMsg = if (errMsg.contains("API key not valid", ignoreCase = true)) {
                        "Firebase API Key is invalid. Please configure your custom Firebase Credentials in the Secrets panel."
                    } else {
                        errMsg
                    }
                    _authState.value = AuthState.Error(displayMsg)
                },
                onCodeSent = { verificationId, _ ->
                    _verificationId.value = verificationId
                    _authState.value = AuthState.OtpSent(cleanPhone)
                }
            )
        }
    }

    fun verifyOtp(phoneNumber: String, code: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val digits = phoneNumber.filter { it.isDigit() }
            val cleanPhone = when {
                digits.startsWith("91") && digits.length > 10 -> digits.substring(2).take(10)
                digits.startsWith("0") && digits.length > 10 -> digits.substring(1).take(10)
                else -> digits.take(10)
            }

            val verificationId = _verificationId.value

            if (verificationId == null) {
                _authState.value = AuthState.Error("Verification ID is missing. Please request a new OTP code.")
                return@launch
            }

            try {
                val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, code)
                val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            val signedIn = repository.loginWithPhone(cleanPhone)
                            if (signedIn) {
                                _authState.value = AuthState.Authenticated
                            } else {
                                _authState.value = AuthState.RegisterRequired(cleanPhone)
                            }
                        }
                    } else {
                        val errMsg = task.exception?.message ?: "Invalid OTP verification code."
                        val displayMsg = if (errMsg.contains("API key not valid", ignoreCase = true)) {
                            "Firebase API Key is invalid. Please configure your custom Firebase Credentials in the Secrets panel."
                        } else {
                            errMsg
                        }
                        _authState.value = AuthState.Error(displayMsg)
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Formulating verification credentials failed.")
            }
        }
    }

    // Sandbox / Demo login bypass (Enables navigating full app without real Firebase custom keys setup)
    fun loginAsDemoUser() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val demoProfile = UserProfile(
                id = "user_curr_demo",
                fullName = "Subramanya Iyer",
                email = "subbu.iyer@demo.com",
                mobileNumber = "9999999999",
                gender = "Male",
                dob = "1995-04-18",
                community = "Iyer",
                subSect = "Vadama",
                education = "B.E. Computer Science",
                occupation = "Senior Architect (Google)",
                location = "Bengaluru",
                height = "5'10\"",
                maritalStatus = "Never Married",
                aboutMe = "Traditional yet modern Vadama Iyer Brahmin. Passionate about spiritual retreats, Sanskrit recitation, machine learning, and organic gardening. Settled in Bengaluru.",
                avatarUrl = "avatar_m1",
                additionalPhotosJson = "[\"avatar_m1_slide1\"]",
                isCurrentUser = true
            )
            repository.registerUser(demoProfile)
            _authState.value = AuthState.Authenticated
        }
    }

    // Email + Password login flows
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            if (email.isEmpty() || password.isEmpty()) {
                _authState.value = AuthState.Error("Please fill in all email and password fields.")
                return@launch
            }
            _authState.value = AuthState.Loading
            
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            val success = repository.loginWithEmail(email, password)
                            if (success) {
                                _authState.value = AuthState.Authenticated
                            } else {
                                _authState.value = AuthState.Error("Account login synced but no local Brahmin Profile found in SQLite database.")
                            }
                        }
                    } else {
                        val errMsg = task.exception?.message ?: "Invalid credentials."
                        val displayMsg = if (errMsg.contains("API key not valid", ignoreCase = true)) {
                            "Firebase API Key is invalid. Please configure your custom Firebase Credentials in the Secrets panel."
                        } else {
                            errMsg
                        }
                        _authState.value = AuthState.Error(displayMsg)
                    }
                }
            } catch (e: Exception) {
                val success = repository.loginWithEmail(email, password)
                if (success) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(e.message ?: "Account not found in local database.")
                }
            }
        }
    }

    // New Profile Registration
    fun registerNewProfile(
        fullName: String,
        gender: String,
        dob: String,
        mobileNumber: String,
        email: String,
        community: String,
        subSect: String,
        education: String,
        occupation: String,
        location: String,
        height: String,
        maritalStatus: String,
        aboutMe: String,
        avatarUrl: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (fullName.isBlank() || dob.isBlank() || email.isBlank() || mobileNumber.isBlank()) {
                _authState.value = AuthState.Error("Please fill in Name, DOB, Email, and Mobile Number.")
                return@launch
            }

            val newProfile = UserProfile(
                id = "user_curr_" + UUID.randomUUID().toString().take(6),
                fullName = fullName,
                email = email,
                mobileNumber = mobileNumber,
                gender = gender,
                dob = dob,
                community = community,
                subSect = subSect,
                education = education,
                occupation = occupation,
                location = location,
                height = height,
                maritalStatus = maritalStatus,
                aboutMe = aboutMe,
                avatarUrl = avatarUrl,
                additionalPhotosJson = "[\"$avatarUrl\"]",
                isCurrentUser = true
            )

            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                if (auth.currentUser == null) {
                    android.util.Log.i("AuthViewModel", "Creating FirebaseAuth credential for registered email.")
                    auth.createUserWithEmailAndPassword(email, "vvsP@ss123").addOnCompleteListener { task ->
                        viewModelScope.launch {
                            repository.registerUser(newProfile)
                            _authState.value = AuthState.Authenticated
                        }
                    }
                    return@launch
                }
            } catch (e: Exception) {
                // Fallback to local DB
            }

            repository.registerUser(newProfile)
            _authState.value = AuthState.Authenticated
        }
    }

    fun submitForgotPassword(email: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            if (email.isEmpty()) {
                onResult("Please enter your email address.")
                return@launch
            }
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult("A password reset link has been dispatched to $email successfully!")
                        } else {
                            onResult(task.exception?.message ?: "Failed submitting password reset.")
                        }
                    }
            } catch (e: Exception) {
                onResult("Failed resetting password. Connect to Firebase first.")
            }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Initial
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                // Ignore
            }
            repository.logout()
            _authState.value = AuthState.Initial
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.delete()
            } catch (e: Exception) {
                // Ignore
            }
            repository.deleteCurrentUserAccount()
            _authState.value = AuthState.Initial
        }
    }
}

sealed interface AuthState {
    object Initial : AuthState
    object Loading : AuthState
    data class OtpSent(val phoneNumber: String) : AuthState
    data class RegisterRequired(val phoneNumber: String) : AuthState
    object Authenticated : AuthState
    data class Error(val message: String) : AuthState
}

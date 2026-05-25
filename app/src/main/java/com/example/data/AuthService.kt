package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

object AuthService {
    private const val TAG = "AuthService"
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            // Check if user has supplied dynamic environment credentials via Secrets Panel / .env
            val rawApiKey = com.example.BuildConfig.FIREBASE_API_KEY
            val apiKey = if (rawApiKey.isNotEmpty() && rawApiKey != "YOUR_FIREBASE_API_KEY") rawApiKey else ""

            val rawAppId = com.example.BuildConfig.FIREBASE_APP_ID
            val appId = if (rawAppId.isNotEmpty() && rawAppId != "YOUR_FIREBASE_APP_ID") rawAppId else "1:558511905520:android:789001e9724962e21c8035"

            val rawProjectId = com.example.BuildConfig.FIREBASE_PROJECT_ID
            val projectId = if (rawProjectId.isNotEmpty() && rawProjectId != "YOUR_FIREBASE_PROJECT_ID") rawProjectId else "vvs-brahmin-matrimony"

            val rawProjectNum = com.example.BuildConfig.FIREBASE_PROJECT_NUMBER
            val projectNum = if (rawProjectNum.isNotEmpty() && rawProjectNum != "YOUR_FIREBASE_PROJECT_NUMBER") rawProjectNum else "558511905520"

            val rawStorageBucket = com.example.BuildConfig.FIREBASE_STORAGE_BUCKET
            val storageBucket = if (rawStorageBucket.isNotEmpty() && rawStorageBucket != "YOUR_FIREBASE_STORAGE_BUCKET") rawStorageBucket else "vvs-brahmin-matrimony.appspot.com"

            if (apiKey.isNotEmpty()) {
                Log.i(TAG, "Dynamic production Firebase environment variables detected! Re-initializing Firebase with your custom Secrets...")
                try {
                    val defaultApp = FirebaseApp.getInstance()
                    defaultApp.delete()
                } catch (e: Exception) {
                    // No default app existed to delete
                }

                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(appId)
                    .setProjectId(projectId)
                    .setGcmSenderId(projectNum)
                    .setStorageBucket(storageBucket)
                    .build()

                FirebaseApp.initializeApp(context.applicationContext, options)
                isInitialized = true
                Log.i(TAG, "Firebase successfully initialized in live production mode with your dynamically supplied console Credentials!")
            } else {
                // If there are no production secrets, just let standard Firebase initialize with google-services.json.
                // We do NOT use any simulator flags or offline simulated bypasses.
                FirebaseApp.initializeApp(context.applicationContext)
                isInitialized = true
                Log.d(TAG, "Standard Firebase initialization completed using google-services.json assets.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Core Initialization failed: ${e.message}", e)
            isInitialized = true
        }
    }

    fun getFirebaseAuth(context: Context): FirebaseAuth {
        initialize(context)
        return FirebaseAuth.getInstance()
    }

    fun verifyPhoneNumber(
        activity: Activity,
        phoneNumber: String,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (FirebaseException) -> Unit,
        onCodeSent: (String, PhoneAuthProvider.ForceResendingToken) -> Unit
    ) {
        initialize(activity.applicationContext)
        val firebaseAuth = getFirebaseAuth(activity.applicationContext)

        // Standard Phone Auth Options setup
        try {
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "onVerificationCompleted: $credential")
                    onVerificationCompleted(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "onVerificationFailed: ${e.message}", e)
                    onVerificationFailed(e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(TAG, "onCodeSent: $verificationId")
                    onCodeSent(verificationId, token)
                }
            }

            val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"

            Log.i(TAG, "Initiating REAL Phone Authentication for: $formattedPhone")
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed initiating verifyPhoneNumber: ${e.message}", e)
            onVerificationFailed(FirebaseException("Failed invoking Firebase Phone Auth SDK: ${e.message}"))
        }
    }
}

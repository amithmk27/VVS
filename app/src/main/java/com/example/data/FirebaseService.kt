package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.messaging.FirebaseMessaging

object FirebaseService {
    private const val TAG = "FirebaseService"

    /**
     * Retrieves an instance of Firebase Firestore.
     */
    fun getFirestore(context: Context): FirebaseFirestore? {
        AuthService.initialize(context.applicationContext)
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Failed retrieving Firestore client instance: ${e.message}")
            null
        }
    }

    /**
     * Retrieves an instance of Firebase Storage.
     */
    fun getStorage(context: Context): FirebaseStorage? {
        AuthService.initialize(context.applicationContext)
        return try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Failed retrieving Storage client instance: ${e.message}")
            null
        }
    }

    /**
     * Initializes and retrieves Firebase Messaging token for FCM notification alerts.
     */
    fun getFCMToken(context: Context, onTokenReceived: (String?) -> Unit) {
        AuthService.initialize(context.applicationContext)
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.i(TAG, "FCM Device Token: $token")
                    onTokenReceived(token)
                } else {
                    Log.w(TAG, "FCM Token retrieval failed: ${task.exception?.message}")
                    onTokenReceived(null)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "FCM messaging registration task failed: ${e.message}")
            onTokenReceived(null)
        }
    }
}

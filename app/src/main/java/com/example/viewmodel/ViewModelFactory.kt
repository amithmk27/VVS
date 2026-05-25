package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.repository.AppRepository

class ViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(repository) as T
            }
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                UserViewModel(repository) as T
            }
            modelClass.isAssignableFrom(MatchViewModel::class.java) -> {
                MatchViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                // Initialize Firebase Core setup & services dynamically
                com.example.data.AuthService.initialize(context.applicationContext)

                val database = AppDatabase.getDatabase(context)
                val repository = AppRepository(
                    profileDao = database.profileDao(),
                    matchDao = database.matchDao(),
                    chatDao = database.chatDao()
                )
                val instance = ViewModelFactory(repository)
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.DatabaseInitializer
import com.example.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-Edge display support
        enableEdgeToEdge()

        // Background seeding of Brahmin profiles on launch
        val database = AppDatabase.getDatabase(applicationContext)
        lifecycleScope.launch {
            DatabaseInitializer.seedDatabaseIfEmpty(database)
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Centralized ViewModel Instantiations via Factory
                    val factory = ViewModelFactory.getInstance(applicationContext)
                    val authViewModel: AuthViewModel = viewModel(factory = factory)
                    val userViewModel: UserViewModel = viewModel(factory = factory)
                    val matchViewModel: MatchViewModel = viewModel(factory = factory)
                    val chatViewModel: ChatViewModel = viewModel(factory = factory)

                    // Flows for auto state logins
                    val currentUser by authViewModel.currentUser.collectAsState(initial = null)

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        // 1. Splash Screen
                        composable("splash") {
                            SplashScreen(
                                currentUser = currentUser,
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onNavigateToHome = {
                                    navController.navigate("dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Login Screen
                        composable("login") {
                            LoginScreen(
                                authViewModel = authViewModel,
                                onNavigateToRegister = { phoneCode ->
                                    val destination = if (phoneCode != null) "register?phone=$phoneCode" else "register"
                                    navController.navigate(destination)
                                },
                                onNavigateToHome = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Register Screen
                        composable(
                            route = "register?phone={phone}",
                            arguments = listOf(
                                navArgument("phone") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val prefilledPhone = backStackEntry.arguments?.getString("phone")
                            RegisterScreen(
                                prefilledPhone = prefilledPhone,
                                authViewModel = authViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToHome = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 4. Main Dashboard Screen (With 4-tab bottom navigation)
                        composable("dashboard") {
                            DashboardScreen(
                                authViewModel = authViewModel,
                                userViewModel = userViewModel,
                                matchViewModel = matchViewModel,
                                chatViewModel = chatViewModel,
                                onNavigateToDetails = { id ->
                                    navController.navigate("profile_details/$id")
                                },
                                onNavigateToMyProfile = {
                                    navController.navigate("profile_self")
                                },
                                onNavigateBackToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 5. Public Profile details Screen
                        composable(
                            route = "profile_details/{profileId}",
                            arguments = listOf(
                                navArgument("profileId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
                            ProfileScreen(
                                profileId = profileId,
                                userViewModel = userViewModel,
                                isSelf = false,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 6. Own Self profile details Editor Screen
                        composable("profile_self") {
                            // Find the logged in user to get their dynamic id (or default token)
                            val selfId = currentUser?.id ?: "current_user_id"
                            ProfileScreen(
                                profileId = selfId,
                                userViewModel = userViewModel,
                                isSelf = true,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.MatchViewModel
import com.example.viewmodel.UserViewModel

@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    matchViewModel: MatchViewModel,
    chatViewModel: ChatViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToMyProfile: () -> Unit,
    onNavigateBackToLogin: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("dashboard_scaffold"),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PurplePrimary
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home Tab"
                        )
                    },
                    label = { Text("Home", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Matches Tab"
                        )
                    },
                    label = { Text("Matches", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("nav_item_matches")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Chat else Icons.Outlined.Chat,
                            contentDescription = "Chats Tab"
                        )
                    },
                    label = { Text("Chats", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("nav_item_chats")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Account Tab"
                        )
                    },
                    label = { Text("Account", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("nav_item_account")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    userViewModel = userViewModel,
                    matchViewModel = matchViewModel,
                    onNavigateToDetails = onNavigateToDetails
                )
                1 -> MatchesScreen(
                    matchViewModel = matchViewModel,
                    onNavigateToDetails = onNavigateToDetails,
                    onStartChat = { partnerId ->
                        // Switch recipient focus and hop over to chat inbox tab (tab indices = 2)
                        chatViewModel.setActiveChatRecipient(partnerId)
                        selectedTab = 2
                    }
                )
                2 -> ChatScreen(
                    chatViewModel = chatViewModel,
                    onNavigateToDetails = onNavigateToDetails
                )
                3 -> AccountScreen(
                    authViewModel = authViewModel,
                    onNavigateToEditProfile = onNavigateToMyProfile,
                    onNavigateBackToLogin = onNavigateBackToLogin
                )
            }
        }
    }
}

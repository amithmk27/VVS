package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MatchRequest
import com.example.model.UserProfile
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.BorderColor
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.TextMedium
import com.example.ui.theme.TextDark
import com.example.viewmodel.MatchViewModel
import com.example.widgets.BrahminAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    matchViewModel: MatchViewModel,
    onNavigateToDetails: (String) -> Unit,
    onStartChat: (String) -> Unit
) {
    var selectedTabState by remember { mutableStateOf(0) } // 0 = Received, 1 = Sent, 2 = Mutual Matches

    val receivedRequests by matchViewModel.receivedInterests.collectAsState()
    val sentRequests by matchViewModel.sentInterests.collectAsState()
    val mutualSuccessList by matchViewModel.mutualMatches.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("matches_screen_root")
    ) {
        // Soft Green Banner top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftGreen)
                .padding(vertical = 18.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "Interests & Matches",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary
            )
        }

        TabRow(
            selectedTabIndex = selectedTabState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GoldAccent, // active line indicator matches Gold Accent
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTabState == 0,
                onClick = { selectedTabState = 0 },
                text = { Text("Received (${receivedRequests.size})", fontWeight = FontWeight.SemiBold) },
                selectedContentColor = GoldAccent,
                unselectedContentColor = TextMedium,
                modifier = Modifier.testTag("tab_received")
            )
            Tab(
                selected = selectedTabState == 1,
                onClick = { selectedTabState = 1 },
                text = { Text("Sent (${sentRequests.size})", fontWeight = FontWeight.SemiBold) },
                selectedContentColor = GoldAccent,
                unselectedContentColor = TextMedium,
                modifier = Modifier.testTag("tab_sent")
            )
            Tab(
                selected = selectedTabState == 2,
                onClick = { selectedTabState = 2 },
                text = { Text("Matches (${mutualSuccessList.size})", fontWeight = FontWeight.SemiBold) },
                selectedContentColor = GoldAccent,
                unselectedContentColor = TextMedium,
                modifier = Modifier.testTag("tab_mutual")
            )
        }

        when (selectedTabState) {
            0 -> {
                // Received Tab
                if (receivedRequests.isEmpty()) {
                    MatchesEmptyState(
                        icon = Icons.Filled.Inbox,
                        title = "No requests received yet",
                        subtitle = "Elders and prospects reviewing matches often send interests. Complete your profile details to be invited!"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(receivedRequests, key = { it.first.id }) { (req, user) ->
                            RequestReceivedCard(
                                user = user,
                                requestStatus = req.status,
                                onViewDetails = { onNavigateToDetails(user.id) },
                                onAccept = {
                                    matchViewModel.acceptInterest(req.id)
                                    Toast.makeText(context, "Matched successfully! Start private chat.", Toast.LENGTH_LONG).show()
                                },
                                onDecline = {
                                    matchViewModel.rejectInterest(req.id)
                                    Toast.makeText(context, "Declined connection request.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            1 -> {
                // Sent Tab
                if (sentRequests.isEmpty()) {
                    MatchesEmptyState(
                        icon = Icons.Filled.Send,
                        title = "No outward interests sent",
                        subtitle = "Explore Brahmin brides/grooms profiles on the home dashboard and click 'Send Interest' to connect!"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sentRequests, key = { it.first.id }) { (req, user) ->
                            RequestSentCard(
                                user = user,
                                requestStatus = req.status,
                                onViewDetails = { onNavigateToDetails(user.id) },
                                onCancel = {
                                    matchViewModel.cancelInterest(req.id)
                                    Toast.makeText(context, "Interest invitation canceled.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            2 -> {
                // Mutual Matches Tab
                if (mutualSuccessList.isEmpty()) {
                    MatchesEmptyState(
                        icon = Icons.Filled.Favorite,
                        title = "No mutual matches yet",
                        subtitle = "When both prospects agree to chat, your status is promoted to a mutual match. Keep exploring!"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mutualSuccessList, key = { it.id }) { user ->
                            MutualMatchCard(
                                user = user,
                                onViewProfile = { onNavigateToDetails(user.id) },
                                onStartChat = { onStartChat(user.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchesEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RequestReceivedCard(
    user: UserProfile,
    requestStatus: String,
    onViewDetails: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrahminAvatar(avatarUrl = user.avatarUrl, fullName = user.fullName, size = 56.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PurplePrimary)
                    Text("${user.community} Brahmin, ${user.subSect}", fontSize = 12.sp, color = Color.Gray)
                    Text("${user.education}  |  ${user.location}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Profile")
                }
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                        .size(40.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Decline", tint = Color.Red)
                }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Accept", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept Pitch", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RequestSentCard(
    user: UserProfile,
    requestStatus: String,
    onViewDetails: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrahminAvatar(avatarUrl = user.avatarUrl, fullName = user.fullName, size = 56.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${user.community} • ${user.subSect}", fontSize = 12.sp, color = Color.Gray)
                
                Surface(
                    color = when (requestStatus) {
                        "PENDING" -> Color(0xFFFEF3C7) // soft amber-50
                        "REJECTED" -> Color(0xFFFFEBEE)
                        else -> Color(0xFFE8F5E9)
                    },
                    contentColor = when (requestStatus) {
                        "PENDING" -> Color(0xFFB45309) // text-amber-700
                        "REJECTED" -> Color(0xFFC62828)
                        else -> Color(0xFF2E7D32)
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = when (requestStatus) {
                            "PENDING" -> "Pending Approval"
                            "REJECTED" -> "Declined"
                            else -> "Accepted"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Cancel invitation", tint = Color.Gray)
                }
                TextButton(onClick = onViewDetails) {
                    Text("Details", fontSize = 12.sp, color = PurplePrimary)
                }
            }
        }
    }
}

@Composable
fun MutualMatchCard(
    user: UserProfile,
    onViewProfile: () -> Unit,
    onStartChat: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrahminAvatar(avatarUrl = user.avatarUrl, fullName = user.fullName, size = 60.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PurplePrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Filled.Verified, contentDescription = "Verified Profile", tint = GoldAccent, modifier = Modifier.size(16.dp))
                    }
                    Text("${user.community} Brahmin, ${user.subSect}", fontSize = 12.sp, color = Color.Gray)
                    Text("${user.occupation} (${user.location})", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewProfile,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Bio")
                }
                Button(
                    onClick = onStartChat,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = "Chat icon", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

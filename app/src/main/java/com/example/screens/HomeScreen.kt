package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.BorderColor
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.TextMedium
import com.example.ui.theme.TextDark
import com.example.ui.theme.BackgroundCream
import com.example.viewmodel.MatchViewModel
import com.example.viewmodel.UserViewModel
import com.example.widgets.BrahminAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    matchViewModel: MatchViewModel,
    onNavigateToDetails: (String) -> Unit
) {
    val profiles by userViewModel.profilesState.collectAsState()
    val searchQuery by userViewModel.searchQuery.collectAsState()
    val selectedCommunity by userViewModel.communityFilter.collectAsState()
    val context = LocalContext.current

    val communityPills = listOf("All", "Smartha", "Madhwa", "Iyengar", "Iyer", "Havyaka", "Shivalli")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_root")
    ) {
        // Upper search and filters strip - elegant light style
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(bottom = 14.dp)
        ) {
            Text(
                text = "Find Your Soulmate",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary, // Sage Green
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Soft Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { userViewModel.setSearchQuery(it) },
                placeholder = { Text("Search by name, job, or degree...", color = TextMedium) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = PurplePrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = BackgroundCream,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_search_bar")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Elegant Filter Chips in horizontal scroller
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(communityPills) { pill ->
                    val isActive = if (pill == "All") selectedCommunity == null else selectedCommunity == pill
                    FilterChip(
                        selected = isActive,
                        onClick = {
                            if (pill == "All") {
                                userViewModel.setCommunityFilter(null)
                            } else {
                                userViewModel.setCommunityFilter(pill)
                            }
                        },
                        label = { Text(pill, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = SoftGreen,
                            labelColor = TextDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isActive,
                            borderColor = BorderColor,
                            selectedBorderColor = PurplePrimary,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
            }
        }

        // List Profiles
        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.SupervisorAccount,
                        contentDescription = "No partners found",
                        tint = Color.Gray,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No compatible profiles active representing this match query.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = {
                        userViewModel.setSearchQuery("")
                        userViewModel.setCommunityFilter(null)
                        userViewModel.setLocationFilter(null)
                    }) {
                        Text("Reset Search Filters", color = PurplePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ProfileMatrimonyCard(
                        profile = profile,
                        matchViewModel = matchViewModel,
                        onShortlistToggle = {
                            userViewModel.toggleShortlist(profile.id, !profile.isShortlisted)
                        },
                        onViewDetails = {
                            onNavigateToDetails(profile.id)
                        },
                        onSendInterest = {
                            matchViewModel.sendInterest(profile.id)
                            Toast.makeText(context, "Interest request dispatched successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMatrimonyCard(
    profile: UserProfile,
    matchViewModel: MatchViewModel,
    onShortlistToggle: () -> Unit,
    onViewDetails: () -> Unit,
    onSendInterest: () -> Unit
) {
    val matchStatus by matchViewModel.getMatchStatusWith(profile.id).collectAsState(initial = null)
    
    // Dynamic Age Computation relative to current artificial year 2026
    val birthYear = profile.dob.split("-").firstOrNull()?.toIntOrNull() ?: 1995
    val currentYear = 2026
    val age = currentYear - birthYear

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_card_${profile.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Upper Info Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrahminAvatar(
                    avatarUrl = profile.avatarUrl,
                    fullName = profile.fullName,
                    size = 72.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = profile.fullName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFFEF3C7), // bg-amber-50
                                contentColor = Color(0xFFB45309), // text-amber-700
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)), // border-amber-200
                            ) {
                                Text(
                                    text = "GOLD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        // Shortlist Heart icon
                        IconButton(onClick = onShortlistToggle) {
                            Icon(
                                imageVector = if (profile.isShortlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Shortlist Icon",
                                tint = if (profile.isShortlisted) Color.Red else Color.LightGray
                            )
                        }
                    }

                    // Sect and Age Details
                    Text(
                        text = "${profile.community} Brahmin, ${profile.subSect}  •  $age Yrs  •  ${profile.height}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Edu role
                    Text(
                        text = "${profile.education}  •  ${profile.occupation}",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = GoldAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = profile.location,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Sect / Community Tags Row in Natural Tones theme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(profile.subSect, profile.education, profile.location).take(3).forEach { tag ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub bio quote
            Text(
                text = "\"${profile.aboutMe}\"",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color.LightGray.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(10.dp))

            // Lower Action CTAs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View Profile Details button - elegant outlined gold button
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Shortlist", fontWeight = FontWeight.SemiBold)
                }

                // Match request dispatch status action buttons
                when {
                    matchStatus == "SENT_PENDING" -> {
                        Button(
                            onClick = {},
                            enabled = false,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftGreen,
                                contentColor = TextMedium
                            ),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Interest Sent")
                        }
                    }
                    matchStatus == "SENT_ACCEPTED" || matchStatus == "RECEIVED_ACCEPTED" -> {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = "Match", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Matched")
                        }
                    }
                    else -> {
                        Button(
                            onClick = onSendInterest,
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Interest", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Connect", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

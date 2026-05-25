package com.example.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextDark
import com.example.viewmodel.UserViewModel
import com.example.widgets.BrahminAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileId: String,
    userViewModel: UserViewModel,
    isSelf: Boolean = false,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val profileToDisplay by userViewModel.getProfileFlow(profileId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isSelf) "My Vedic Profile" else (profileToDisplay?.fullName ?: "Profile Details"),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurplePrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padValues ->
        if (profileToDisplay == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PurplePrimary)
            }
        } else {
            ProfileDetailsContent(
                profile = profileToDisplay!!,
                isSelf = isSelf,
                userViewModel = userViewModel,
                innerPadding = padValues
            )
        }
    }
}

@Composable
fun ProfileDetailsContent(
    profile: UserProfile,
    isSelf: Boolean,
    userViewModel: UserViewModel,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }

    // Form editing states
    var editedName by remember(profile.fullName) { mutableStateOf(profile.fullName) }
    var editedSect by remember(profile.subSect) { mutableStateOf(profile.subSect) }
    var editedEdu by remember(profile.education) { mutableStateOf(profile.education) }
    var editedOcc by remember(profile.occupation) { mutableStateOf(profile.occupation) }
    var editedLoc by remember(profile.location) { mutableStateOf(profile.location) }
    var editedAbout by remember(profile.aboutMe) { mutableStateOf(profile.aboutMe) }
    var editedHeight by remember(profile.height) { mutableStateOf(profile.height) }

    // Simulated astrological properties which add great community fidelity!
    val birthYear = profile.dob.split("-").firstOrNull()?.toIntOrNull() ?: 1995
    val currentYear = 2026
    val calculatedAge = currentYear - birthYear

    // Horoscope mappings based on Name hash for deterministic high-fidelity mock data!
    val rashiOptions = listOf("Mesha", "Vrishabha", "Mithuna", "Karka", "Simha", "Kanya", "Tula", "Vrishchika", "Dhanu", "Makara", "Kumbha", "Meena")
    val nakshatraOptions = listOf("Ashwini", "Rohini", "Arudra", "Pushya", "Uttara", "Chitra", "Swati", "Anuradha", "Moola", "Shravana", "Revati")
    
    val hash = profile.fullName.hashCode().coerceAtLeast(0)
    val assignedRashi = rashiOptions[hash % rashiOptions.size]
    val assignedNakshat = nakshatraOptions[hash % nakshatraOptions.size]
    val isManglik = if (hash % 3 == 0) "Yes" else "No (Anukul Match)"
    val gothra = if (hash % 2 == 0) "Kashyapa" else "Srivatsa"

    // Parse album list
    val albumList = userViewModel.parseAlbumPhotos(profile.additionalPhotosJson)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture & Name Badge
        Box(
            modifier = Modifier.padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            BrahminAvatar(avatarUrl = profile.avatarUrl, fullName = profile.fullName, size = 100.dp)
            if (isSelf) {
                // Change main image CTA button
                IconButton(
                    onClick = {
                        // Pick next random preset avatar representation to simulate Image Upload
                        val presets = if (profile.gender == "Male") {
                            listOf("avatar_m1", "avatar_m2", "avatar_m3")
                        } else {
                            listOf("avatar_f1", "avatar_f2", "avatar_f3")
                        }
                        val next = presets.filter { it != profile.avatarUrl }.randomOrNull() ?: presets.first()
                        userViewModel.uploadProfileImage(next)
                        Toast.makeText(context, "Profile picture updated from simulated phone gallery!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera", tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (!isEditing) {
            Text(profile.fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
            Text(
                text = "${profile.community} Brahmin, ${profile.subSect} • Gothra: $gothra",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )

            // Self Edit Trigger
            if (isSelf) {
                Button(
                    onClick = { isEditing = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Modify Profile Bio")
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            // Inline Editor Form
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("edit_fullName")
            )
            OutlinedTextField(
                value = editedSect,
                onValueChange = { editedSect = it },
                label = { Text("Sub-Sect / Gothra") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = editedEdu,
                onValueChange = { editedEdu = it },
                label = { Text("Education") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = editedOcc,
                onValueChange = { editedOcc = it },
                label = { Text("Occupation") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = editedLoc,
                onValueChange = { editedLoc = it },
                label = { Text("Location") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = editedHeight,
                onValueChange = { editedHeight = it },
                label = { Text("Height") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = editedAbout,
                onValueChange = { editedAbout = it },
                label = { Text("About Me Summary") },
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { isEditing = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val updated = profile.copy(
                            fullName = editedName,
                            subSect = editedSect,
                            education = editedEdu,
                            occupation = editedOcc,
                            location = editedLoc,
                            height = editedHeight,
                            aboutMe = editedAbout
                        )
                        userViewModel.updateSelfProfile(updated)
                        isEditing = false
                        Toast.makeText(context, "Auspicious profile saved successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 1: Astrological horoscope configurations
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BorderColor),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Language, contentDescription = "Horoscope", tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vedic Astrological Match", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PurplePrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Janma Rashi", fontSize = 12.sp, color = Color.Gray)
                        Text(assignedRashi, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nakshatra", fontSize = 12.sp, color = Color.Gray)
                        Text(assignedNakshat, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Manglik / Chevvai", fontSize = 12.sp, color = Color.Gray)
                        Text(isManglik, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gotra", fontSize = 12.sp, color = Color.Gray)
                        Text(gothra, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Section 2: Education & Location Details
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BorderColor),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Background & Education", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PurplePrimary)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Degree", value = profile.education)
                DetailRow(label = "Occupation", value = profile.occupation)
                DetailRow(label = "Location", value = profile.location)
                DetailRow(label = "Birth Date", value = profile.dob)
                DetailRow(label = "Age Weight", value = "$calculatedAge Yrs")
                DetailRow(label = "Height Spec", value = profile.height)
                DetailRow(label = "Marital Status", value = profile.maritalStatus)
            }
        }

        // Section 3: Professional Album uploading simulation
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BorderColor),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Matrimonial Album", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PurplePrimary)
                    if (isSelf) {
                        TextButton(
                            onClick = {
                                val galleryPresets = listOf("avatar_m1", "avatar_m2", "avatar_m3", "avatar_f1", "avatar_f2", "avatar_f3")
                                val pick = galleryPresets.random()
                                userViewModel.addAlbumPhoto(pick)
                                Toast.makeText(context, "Added simulated photo to album!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("+ Add Photo", color = PurplePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (albumList.isEmpty()) {
                    Text("No additional images uploaded to this profile's bio album.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        albumList.forEach { photoToken ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                // Draw actual Avatar as photo
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    BrahminAvatar(avatarUrl = photoToken, fullName = profile.fullName, size = 64.dp)
                                }
                                
                                if (isSelf) {
                                    // Delete picture button
                                    IconButton(
                                        onClick = {
                                            userViewModel.deleteAlbumPhoto(photoToken)
                                            Toast.makeText(context, "Removed photo from album.", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Icon", tint = Color.Red, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        Text(text = value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
    }
}

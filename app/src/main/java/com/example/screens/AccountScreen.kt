package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextDark
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.BorderColor
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    authViewModel: AuthViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateBackToLogin: () -> Unit
) {
    val context = LocalContext.current

    var notificationMatchAlerts by remember { mutableStateOf(true) }
    var notificationChatAlerts by remember { mutableStateOf(true) }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("account_screen_root")
    ) {
        // Soft Green Banner top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftGreen)
                .padding(vertical = 18.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "Account Configurations",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Core profile shortcut
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("My Profile Details", fontWeight = FontWeight.Bold, color = PurplePrimary, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToEditProfile)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = PurplePrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("View & Edit My Brahmin Bio", fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            // Section 2: Security settings
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Security Credentials", fontWeight = FontWeight.Bold, color = PurplePrimary, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPasswordDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = PurplePrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Change Password Details", fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            // Section 3: Notification Toggles
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vedic Alerts Settings", fontWeight = FontWeight.Bold, color = PurplePrimary, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Matching Proposal Invites", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = notificationMatchAlerts,
                            onCheckedChange = { notificationMatchAlerts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = PurplePrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Instant Messages Alerts", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = notificationChatAlerts,
                            onCheckedChange = { notificationChatAlerts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = PurplePrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Section 4: Exit Actions (Logout & Delete Account)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        authViewModel.logout()
                        onNavigateBackToLogin()
                        Toast.makeText(context, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("logout_button")
                ) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out From Account", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("delete_account_button")
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = "Delete Account")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Permanently Delete My Bio Profile", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Modal dialogue for changing password
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Password Details") },
            text = {
                Column {
                    Text("Verify current old password and specify your new security credentials.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Old Password") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (oldPass.isNotBlank() && newPass.isNotBlank()) {
                            Toast.makeText(context, "Auspicious password updated successfully!", Toast.LENGTH_LONG).show()
                            showPasswordDialog = false
                            oldPass = ""
                            newPass = ""
                        } else {
                            Toast.makeText(context, "Password fields cannot be empty.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Apply Change", color = PurplePrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Account Deletion Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Matrimonial Account?") },
            text = { Text("Are you absolutely sure you wish to delete your profile? This will immediately remove all biodata and matching logs permanently. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.deleteAccount()
                        onNavigateBackToLogin()
                        showDeleteDialog = false
                        Toast.makeText(context, "Account permanently removed. Farewell!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                ) {
                    Text("Delete Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Keep My Account", color = Color.Gray)
                }
            }
        )
    }
}

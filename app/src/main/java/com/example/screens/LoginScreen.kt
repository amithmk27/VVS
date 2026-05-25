package com.example.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.DeepGold
import com.example.ui.theme.TextMedium
import com.example.ui.theme.SoftGreen
import com.example.viewmodel.AuthState
import com.example.viewmodel.AuthViewModel
import com.example.widgets.BrahminAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: (String?) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0 = Mobile Number OTP, 1 = Email + Password

    var mobileNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    // Navigation triggers
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                onNavigateToHome()
            }
            is AuthState.RegisterRequired -> {
                // Redirect user to registration screen with premade phone token!
                onNavigateToRegister(state.phoneNumber)
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.clearError()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFDF9), // warm off-white touch of gold
                        Color(0xFFF8FAF5)  // premium background sage-cream
                    )
                )
            )
            .testTag("login_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Custom Banner with 3 Acharya Portraits with golden borders and soft shadows (Bigger and fully responsive)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                border = BorderStroke(1.5.dp, GoldAccent.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PurplePrimary.copy(alpha = 0.08f), Color.White)
                            )
                        )
                        .padding(vertical = 24.dp), // Increased padding for bigger banner section
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        listOf(
                            R.drawable.shri_adi_shankaracharya to "Adi Shankaracharya",
                            R.drawable.shri_ramanandacharya to "Ramanandacharya",
                            R.drawable.shri_madhvacharya to "Madhvacharya"
                        ).forEach { (resId, name) ->
                            Card(
                                elevation = CardDefaults.cardElevation(6.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, GoldAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f) // Consistent elegant circular proportion
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(SoftGreen)
                                        .padding(4.dp), // Frame mount effect to view full portrait cleanly
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = "Portrait of $name",
                                        contentScale = ContentScale.Fit, // Fits completely without any cropping
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Header Typography Styling
            Text(
                text = "VVS Brahmin Matrimony",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "“A Platform by Brahmins, for Brahmins”",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DeepGold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Connecting Brahmin families with trust and tradition",
                fontSize = 13.sp,
                color = TextMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Dynamic Form Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (authState !is AuthState.OtpSent) {
                        // Regular Toggles for Mobile or Email
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Mobile OTP", fontWeight = FontWeight.SemiBold) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Email Login", fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }

                    when (val state = authState) {
                        is AuthState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PurplePrimary)
                            }
                        }

                        is AuthState.OtpSent -> {
                            // Render direct OTP entry block
                            Text(
                                text = "OTP Code Sent!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "We have dispatched a validation OTP pin to ${state.phoneNumber}. Please enter the 6-digit OTP code below to continue.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6) otpCode = it },
                                label = { Text("6-Digit OTP") },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "OTP") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_code_textfield")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { authViewModel.verifyOtp(state.phoneNumber, otpCode) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("verify_otp_button")
                            ) {
                                Text("Verify & Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = {
                                    val act = context as? android.app.Activity
                                    if (act != null) {
                                        authViewModel.sendOtp(act, state.phoneNumber)
                                    }
                                }
                            ) {
                                Text("Resend OTP", color = PurplePrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        else -> {
                            if (selectedTab == 0) {
                                // Mobile login tab
                                OutlinedTextField(
                                    value = mobileNumber,
                                    onValueChange = { input ->
                                        val digits = input.filter { it.isDigit() }
                                        mobileNumber = when {
                                            digits.startsWith("91") && digits.length > 10 -> digits.substring(2).take(10)
                                            digits.startsWith("0") && digits.length > 10 -> digits.substring(1).take(10)
                                            else -> digits.take(10)
                                        }
                                    },
                                    label = { Text("Mobile Number") },
                                    placeholder = { Text("Enter 10 digit number") },
                                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Mobile") },
                                    prefix = { Text("+91 ") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("mobile_number_textfield")
                                 )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        val act = context as? android.app.Activity
                                        if (act != null) {
                                            authViewModel.sendOtp(act, mobileNumber)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurpleSecondary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("send_otp_button")
                                ) {
                                    Text("Send Verification OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            } else {
                                // Email login tab
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("email_textfield")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Filled.Security, contentDescription = "Password") },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("password_textfield")
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PurplePrimary,
                                        modifier = Modifier
                                            .clickable { showForgotDialog = true }
                                            .testTag("forgot_password_button")
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { authViewModel.loginWithEmail(email, password) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("email_login_submit")
                                ) {
                                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    // Sandbox Mode Bypass Divider & Button
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                        Text(
                            text = " OR ",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMedium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { authViewModel.loginAsDemoUser() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PurplePrimary
                        ),
                        border = BorderStroke(1.5.dp, GoldAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("demo_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Demo Mode",
                            tint = GoldAccent,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Explore App in Sandbox Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PurplePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action line toggling registration
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New to our community ? ",
                    color = TextMedium,
                    fontSize = 15.sp
                )
                Text(
                    text = "Register Free",
                    color = GoldAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToRegister(null) }
                        .testTag("register_toggle_button")
                )
            }
        }
    }

    // Modal dialogue for forgotten password
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your email address to verify community credentials and fetch password update instructions.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.submitForgotPassword(forgotEmail) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                        showForgotDialog = false
                    }
                ) {
                    Text("Send Recovery", color = PurplePrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

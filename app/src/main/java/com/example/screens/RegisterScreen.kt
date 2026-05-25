package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.viewmodel.AuthState
import com.example.viewmodel.AuthViewModel
import com.example.widgets.BrahminAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    prefilledPhone: String?,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    // Fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf(prefilledPhone ?: "") }
    var gender by remember { mutableStateOf("Female") } // default Female
    var dob by remember { mutableStateOf("1996-06-15") } // Default representation
    
    var selectedCommunity by remember { mutableStateOf("Smartha") }
    var subSect by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("5'4\"") }
    var maritalStatus by remember { mutableStateOf("Never Married") }
    var aboutMe by remember { mutableStateOf("") }

    // Avatar selections
    val maleAvatars = listOf("avatar_m1", "avatar_m2", "avatar_m3")
    val femaleAvatars = listOf("avatar_f1", "avatar_f2", "avatar_f3")
    var selectedAvatar by remember { mutableStateOf("avatar_f1") }

    // Auto update default avatar if gender toggles
    LaunchedEffect(gender) {
        selectedAvatar = if (gender == "Male") maleAvatars.first() else femaleAvatars.first()
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onNavigateToHome()
        } else if (authState is AuthState.Error) {
            val errorMsg = (authState as AuthState.Error).message
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register New Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("register_back_button")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurplePrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section 1: Traditional Avatar Selector
            Text(
                text = "Choose Traditional Avatar Picture",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PurplePrimary,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentAvatarsList = if (gender == "Male") maleAvatars else femaleAvatars
                currentAvatarsList.forEach { av ->
                    val isSelected = selectedAvatar == av
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .clickable { selectedAvatar = av }
                            .border(
                                width = if (isSelected) 4.dp else 1.dp,
                                color = if (isSelected) GoldAccent else Color.LightGray,
                                shape = CircleShape
                            )
                    ) {
                        BrahminAvatar(
                            avatarUrl = av,
                            fullName = if (fullName.isBlank()) "V" else fullName,
                            size = 64.dp
                        )
                    }
                }
            }

            // Section 2: Essential Particulars
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Private & Core Information",
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Name") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_fullname")
                    )

                    // Gender Selector Checkboxes
                    Text(
                        text = "Gender",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { gender = "Female" }.weight(1f)
                        ) {
                            RadioButton(selected = gender == "Female", onClick = { gender = "Female" })
                            Text("Female Profile")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { gender = "Male" }.weight(1f)
                        ) {
                            RadioButton(selected = gender == "Male", onClick = { gender = "Male" })
                            Text("Male Profile")
                        }
                    }

                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth (YYYY-MM-DD)") },
                        leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = "DOB") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_dob")
                    )

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Mobile Number") },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Mobile") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_phone")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_email")
                    )
                }
            }

            // Section 3: Spiritual & Community Particulars
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Community & Spiritual Details",
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Brahmin Community Sect",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Compact select row for communities to save layout complexity & allow immediate clicking!
                    val communities = listOf("Smartha", "Madhwa", "Iyengar", "Iyer", "Havyaka", "Shivalli", "Kota", "Others")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        var expandedComm by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { expandedComm = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedCommunity, fontWeight = FontWeight.Bold, color = PurplePrimary)
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select")
                                }
                            }
                            DropdownMenu(
                                expanded = expandedComm,
                                onDismissRequest = { expandedComm = false }
                            ) {
                                communities.forEach { comm ->
                                    DropdownMenuItem(
                                        text = { Text(comm) },
                                        onClick = {
                                            selectedCommunity = comm
                                            expandedComm = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = subSect,
                        onValueChange = { subSect = it },
                        label = { Text("Sub-sect / Gothra Details") },
                        placeholder = { Text("e.g. Mulukanadu / Srivatsa") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_subsect")
                    )
                }
            }

            // Section 4: Professional & Lifestyle Characteristics
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Education, Career & Background",
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = education,
                        onValueChange = { education = it },
                        label = { Text("Education Degree") },
                        placeholder = { Text("e.g. M.Tech / MBA / B.E.") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_edu")
                    )

                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("Occupation / Job Role") },
                        placeholder = { Text("e.g. Software Engineer / Priest") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_occ")
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Job Location (Resident City)") },
                        placeholder = { Text("e.g. Bengaluru") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_loc")
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height") },
                            placeholder = { Text("e.g. 5'6\"") },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        OutlinedTextField(
                            value = maritalStatus,
                            onValueChange = { maritalStatus = it },
                            label = { Text("Marital Status") },
                            placeholder = { Text("Never Married") },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = aboutMe,
                        onValueChange = { aboutMe = it },
                        label = { Text("About Me (Bio summary)") },
                        placeholder = { Text("Introduce yourself, interests, family value system...") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("reg_aboutme")
                    )
                }
            }

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = PurplePrimary, modifier = Modifier.padding(16.dp))
            } else {
                Button(
                    onClick = {
                        // Call ViewModel
                        authViewModel.registerNewProfile(
                            fullName = fullName,
                            gender = gender,
                            dob = dob,
                            mobileNumber = mobileNumber,
                            email = email,
                            community = selectedCommunity,
                            subSect = subSect,
                            education = education,
                            occupation = occupation,
                            location = location,
                            height = height,
                            maritalStatus = maritalStatus,
                            aboutMe = aboutMe,
                            avatarUrl = selectedAvatar
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_registration_button")
                ) {
                    Icon(Icons.Filled.HowToReg, contentDescription = "Submit", modifier = Modifier.padding(end = 8.dp))
                    Text("Complete Vedic Profile Creation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

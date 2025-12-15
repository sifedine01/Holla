package com.sifedin.tinderclone.ui.screens.settings

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.sifedin.tinderclone.ui.theme.HolaPinkPrimary
import com.sifedin.tinderclone.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val isLoading = viewModel.isLoading
    val userProfile = viewModel.currentUserProfile

    var nameInput by viewModel::nameInput
    var birthdayInput by viewModel::birthdayInput
    var genderInput by viewModel::genderInput

    val view = LocalView.current
    SideEffect {
        val window = (view.context as ComponentActivity).window
        window.navigationBarColor = Color.Black.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Edit Profile",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            if (nameInput.isNotBlank()) {
                                viewModel.updateProfile(onBack)
                            }
                        },
                        enabled = !isLoading && nameInput.isNotBlank()
                    ) {
                        Text(
                            "Done",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF520010)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Profile Photo Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape,
                                        spotColor = HolaPinkPrimary.copy(alpha = 0.25f)
                                    )
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(userProfile?.photos?.firstOrNull()),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    HolaPinkPrimary.copy(alpha = 0.1f),
                                                    Color.Gray
                                                )
                                            )
                                        )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                userProfile?.name ?: "Loading...",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                item {
                    // Form Fields
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Personal Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Name") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HolaPinkPrimary,
                                    unfocusedBorderColor = Color(0xFF666666),
                                    focusedLabelColor = HolaPinkPrimary,
                                    unfocusedLabelColor = Color(0xFFCCCCCC),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = HolaPinkPrimary,
                                    focusedContainerColor = Color(0xFF333333),
                                    unfocusedContainerColor = Color(0xFF333333)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = userProfile?.uid ?: "Loading...",
                                onValueChange = { },
                                label = { Text("User ID") },
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0xFF666666),
                                    disabledLabelColor = Color(0xFFCCCCCC),
                                    disabledTextColor = Color.White,
                                    disabledContainerColor = Color(0xFF333333)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = userProfile?.phoneNumber ?: "Not provided",
                                onValueChange = { },
                                label = { Text("Phone") },
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0xFF666666),
                                    disabledLabelColor = Color(0xFFCCCCCC),
                                    disabledTextColor = Color.White,
                                    disabledContainerColor = Color(0xFF333333)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = genderInput?.replaceFirstChar { it.uppercase() } ?: "Select Gender",
                                onValueChange = { },
                                label = { Text("Gender") },
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HolaPinkPrimary,
                                    unfocusedBorderColor = Color(0xFF666666),
                                    focusedLabelColor = HolaPinkPrimary,
                                    unfocusedLabelColor = Color(0xFFCCCCCC),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF333333),
                                    unfocusedContainerColor = Color(0xFF333333)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // TODO: Open gender selection dialog
                                    }
                            )

                            OutlinedTextField(
                                value = birthdayInput.ifEmpty { "Select Date" },
                                onValueChange = { },
                                label = { Text("Date of Birth") },
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HolaPinkPrimary,
                                    unfocusedBorderColor = Color(0xFF666666),
                                    focusedLabelColor = HolaPinkPrimary,
                                    unfocusedLabelColor = Color(0xFFCCCCCC),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF333333),
                                    unfocusedContainerColor = Color(0xFF333333)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // TODO: Open DatePickerDialog
                                    }
                            )
                        }
                    }
                }

                item {
                    // Status Messages
                    viewModel.successMessage?.let { message ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E8)
                            )
                        ) {
                            Text(
                                message,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    viewModel.errorMessage?.let { message ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Text(
                                message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
package com.sifedin.tinderclone.ui.screens.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.sifedin.tinderclone.ui.theme.HolaPinkPrimary
import com.sifedin.tinderclone.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateToEditProfile: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit,
    onLogout: () -> Unit
) {
    val user = viewModel.currentUserProfile

    // Refresh user profile every time this screen is opened
    LaunchedEffect(Unit) {
        viewModel.refreshUserProfile()
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
                            "Settings",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF520010)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
        ) {
            // User header section
            item {
                UserHeader(
                    name = user?.name ?: "Loading...",
                    photoUrl = user?.photos?.firstOrNull(),
                    onEditClick = onNavigateToEditProfile
                )
            }

            // Settings section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = Color(0xFFE0E0E0)
                )

                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Get Notifications",
                    trailingContent = { Switch(checked = false, onCheckedChange = { /* TODO */ }) }
                )
                SettingsItem(
                    icon = Icons.Default.Settings,
                    title = "Account Setting",
                    onClick = { /* TODO: Navigate to Account Settings */ }
                )

                // Logout and Delete options
                SettingsItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    iconColor = Color.Red,
                    titleColor = Color.Red,
                    onClick = { viewModel.logout(onLogout) }
                )
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Account",
                    iconColor = Color.Red,
                    titleColor = Color.Red,
                    onClick = onNavigateToDeleteAccount
                )
            }
        }
    }
}

@Composable
fun UserHeader(name: String, photoUrl: String?, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = HolaPinkPrimary.copy(alpha = 0.25f)
                )
        ) {
            Image(
                painter = rememberAsyncImagePainter(photoUrl),
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                HolaPinkPrimary.copy(alpha = 0.1f),
                                Color.LightGray
                            )
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.width(20.dp))

        Column {
            Text(
                name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HolaPinkPrimary.copy(alpha = 0.1f),
                    contentColor = HolaPinkPrimary
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Edit Profile",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    iconColor: Color = HolaPinkPrimary,
    titleColor: Color = Color(0xFF2C2C2C)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onClick != null, onClick = onClick ?: {})
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (iconColor == Color.Red) {
                                Color.Red.copy(alpha = 0.1f)
                            } else {
                                HolaPinkPrimary.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (titleColor == Color.Red) Color.Red else Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            if (trailingContent != null) {
                trailingContent()
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Navigate",
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
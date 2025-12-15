package com.sifedin.tinderclone.ui.screens.match

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.sifedin.tinderclone.ui.theme.HolaPinkPrimary
import com.sifedin.tinderclone.viewmodel.AuthViewModel

@Composable
fun MatchScreen(
    matchName: String,
    matchId: String,
    onSendMessage: (matchId: String) -> Unit,
    onCancel: () -> Unit
) {
    val authVM: AuthViewModel = viewModel()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "It's a Match ${matchName}!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = HolaPinkPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "You and $matchName have similar minds",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            // Match photos with heart overlay
            Box(
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-20).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserCirclePhoto(imageUrl = null) // Current user photo
                    UserCirclePhoto(imageUrl = "https://via.placeholder.com/120") // Partner photo - will be updated
                }
                
                // Heart icon in the center
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(HolaPinkPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Match",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = { 
                    // Ensure the match is properly created before navigating
                    onSendMessage(matchId)
                },
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HolaPinkPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text(
                    "Send a message",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onCancel) {
                Text(
                    "Cancel",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun UserCirclePhoto(imageUrl: String?) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        HolaPinkPrimary.copy(alpha = 0.1f),
                        Color.LightGray
                    )
                )
            )
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "User photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
package com.sifedin.tinderclone.ui.screens.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.sifedin.tinderclone.data.model.MatchWithUser
import com.sifedin.tinderclone.ui.theme.HolaPinkPrimary
import com.sifedin.tinderclone.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    viewModel: ChatViewModel = viewModel(),
    onChatClicked: (matchId: String, partnerName: String) -> Unit
) {
    val matches = viewModel.matches
    val isLoading = viewModel.matchesLoading

    LaunchedEffect(Unit) {
        viewModel.loadMatches()
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
                            "Chats",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 20.sp
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
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = HolaPinkPrimary
                        )
                    }
                }
                matches.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No chats yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Start matching to begin conversations!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = matches,
                            key = { it.match.id }
                        ) { matchWithUser ->
                            ChatCard(
                                matchWithUser = matchWithUser,
                                onClick = {
                                    onChatClicked(matchWithUser.match.id, matchWithUser.user.name)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatCard(
    matchWithUser: MatchWithUser,
    onClick: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val lastMessage = matchWithUser.match.lastMessage
    val timestamp = matchWithUser.match.lastMessageTimestamp
    val lastMessageSenderId = matchWithUser.match.lastMessageSenderId
    val seenBy = matchWithUser.match.seenBy

    // Check if message is unread (sent by partner and current user hasn't seen it)
    val isUnread = lastMessageSenderId != null &&
            lastMessageSenderId != currentUserId &&
            currentUserId != null &&
            !seenBy.contains(currentUserId)

    // Animated glow effect for unread messages
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Box {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = matchWithUser.user.photos.firstOrNull()
                    ),
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                )

                // Unread indicator dot
                if (isUnread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF4458))
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Chat Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name
                Text(
                    text = matchWithUser.user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Last Message with glow effect if unread
                Text(
                    text = if (!lastMessage.isNullOrBlank()) {
                        if (lastMessage.length > 35) "${lastMessage.take(35)}..." else lastMessage
                    } else {
                        "Start chatting"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isUnread -> Color(0xFFFF4458).copy(alpha = glowAlpha)
                        lastMessage.isNullOrBlank() -> Color(0xFF888888)
                        else -> Color(0xFFCCCCCC)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Time and Status
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (timestamp != null) formatTime(timestamp) else "New",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp
                )

                if (isUnread) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF4458)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Date?): String {
    if (timestamp == null) return "New"

    val now = Date()
    val diff = now.time - timestamp.time

    return when {
        diff < 60_000 -> "now"
        diff < 3600_000 -> "${(diff / 60_000).toInt()}m"
        diff < 86400_000 -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
        }
        diff < 604800_000 -> {
            SimpleDateFormat("EEE", Locale.getDefault()).format(timestamp)
        }
        else -> {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(timestamp)
        }
    }
}
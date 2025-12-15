package com.sifedin.tinderclone.ui.screens.swipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.sifedin.tinderclone.data.model.User
import com.sifedin.tinderclone.ui.theme.HolaPinkPrimary
import com.sifedin.tinderclone.viewmodel.SwipeViewModel
import androidx.navigation.NavController
import com.sifedin.tinderclone.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel = viewModel(),
    navController: NavController
) {
    val matches = viewModel.potentialMatches
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Dating App",
                            tint = HolaPinkPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Holla",
                            fontWeight = FontWeight.Bold,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (error != null) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                } else if (matches.isEmpty()) {
                    Text("No more profiles nearby.", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                } else {
                    SwipeCardStack(
                        matches = matches,
                        onSwipeLeft = { user -> viewModel.recordSwipe(user.uid, isLiked = false) },
                        onSwipeRight = { user -> viewModel.recordSwipe(user.uid, isLiked = true) }
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeCardStack(
    matches: List<User>,
    onSwipeLeft: (User) -> Unit,
    onSwipeRight: (User) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        matches.forEachIndexed { index, user ->
            ProfileCard(
                user = user,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingActionButton(
                onClick = { onSwipeLeft(matches.first()) },
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.Red,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color.Red.copy(alpha = 0.25f)
                    )
            ) {
                Icon(
                    Icons.Filled.Close, 
                    contentDescription = "Nope",
                    modifier = Modifier.size(24.dp)
                )
            }

            FloatingActionButton(
                onClick = { onSwipeRight(matches.first()) },
                containerColor = HolaPinkPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = HolaPinkPrimary.copy(alpha = 0.25f)
                    )
            ) {
                Icon(
                    Icons.Filled.Favorite, 
                    contentDescription = "Like",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileCard(user: User, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(0.75f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            Image(
                painter = rememberAsyncImagePainter(model = user.photos.firstOrNull()),
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "${user.name}, ${user.getAge()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
        }
    }
}
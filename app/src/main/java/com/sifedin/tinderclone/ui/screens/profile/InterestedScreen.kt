package com.sifedin.tinderclone.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sifedin.tinderclone.ui.theme.HolaPinkPrimary
import com.sifedin.tinderclone.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestedScreen(
    viewModel: AuthViewModel,
    onContinue: () -> Unit
) {
    val error = viewModel.errorMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                "Who are you\nInterested In?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = HolaPinkPrimary,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Options List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OptionButton(
                    label = "Men",
                    selected = viewModel.interestedIn == "male",
                    onClick = { viewModel.interestedIn = "male" }
                )
                OptionButton(
                    label = "Women",
                    selected = viewModel.interestedIn == "female",
                    onClick = { viewModel.interestedIn = "female" }
                )
                OptionButton(
                    label = "Everyone",
                    selected = viewModel.interestedIn == "everyone",
                    onClick = { viewModel.interestedIn = "everyone" }
                )
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HolaPinkPrimary,
                    contentColor = Color.White
                ),
                onClick = {
                    if (viewModel.interestedIn == null) {
                        viewModel.errorMessage = "Please select your preference"
                    } else {
                        viewModel.errorMessage = null
                        onContinue()
                    }
                }
            ) {
                Text(
                    "Done",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun OptionButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) HolaPinkPrimary else Color.White,
            contentColor = if (selected) Color.White else HolaPinkPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (selected) 0.dp else 2.dp
        )
    ) {
        Text(
            label,
            fontWeight = FontWeight.SemiBold
        )
    }
}
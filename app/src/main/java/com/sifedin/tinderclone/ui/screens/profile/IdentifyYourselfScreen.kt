package com.sifedin.tinderclone.ui.screens.profile

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sifedin.tinderclone.ui.theme.HolaPinkPrimary
import com.sifedin.tinderclone.viewmodel.AuthViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentifyYourselfScreen(
    viewModel: AuthViewModel,
    onContinue: () -> Unit
) {
    val error = viewModel.errorMessage
    val calendar = Calendar.getInstance()
    val context = LocalContext.current

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            viewModel.birthday = "$day/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR) - 18,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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
                "Identify Yourself",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = HolaPinkPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Introduce yourself fill out the details so people know about you.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Gender Selection
            Text(
                "I am a",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GenderButton(
                    label = "Male",
                    selected = viewModel.gender == "male",
                    onClick = { viewModel.gender = "male" },
                    modifier = Modifier.weight(1f)
                )
                GenderButton(
                    label = "Female",
                    selected = viewModel.gender == "female",
                    onClick = { viewModel.gender = "female" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Other Options
            Text(
                "Other Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Handle other options */ }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "More options",
                    color = HolaPinkPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    ">",
                    color = HolaPinkPrimary,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Birthday
            Text(
                "Birthday",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePicker.show() }
                    .background(
                        Color(0xFF333333),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = viewModel.birthday ?: "Select your birthday",
                    color = if (viewModel.birthday != null) Color.White else Color(0xFFCCCCCC),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Name
            Text(
                "Name",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                placeholder = { Text("Add your name here") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HolaPinkPrimary,
                    unfocusedBorderColor = Color(0xFF666666),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = HolaPinkPrimary,
                    focusedContainerColor = Color(0xFF1A1A1A),
                    unfocusedContainerColor = Color(0xFF1A1A1A)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue Button
            Button(
                onClick = {
                    when {
                        viewModel.name.isBlank() -> viewModel.errorMessage = "Enter your name"
                        viewModel.gender.isNullOrEmpty() -> viewModel.errorMessage = "Select gender"
                        viewModel.birthday.isNullOrEmpty() -> viewModel.errorMessage = "Select birthday"
                        else -> {
                            viewModel.errorMessage = null
                            onContinue()
                        }
                    }
                },
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HolaPinkPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    "Continue",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun GenderButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
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


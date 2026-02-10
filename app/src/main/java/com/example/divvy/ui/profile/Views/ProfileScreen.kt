package com.example.divvy.ui.profile.Views

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.divvy.AuthActivity
import com.example.divvy.components.OutlineButton
import com.example.divvy.ui.auth.Views.AuthBackground
import com.example.divvy.ui.auth.Views.PurplePrimary
import com.example.divvy.ui.auth.Views.PurpleSecondary
import com.example.divvy.ui.profile.ViewModels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile
    val displayEmail = profile?.email ?: uiState.email

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text("Manage profile, linked accounts, and settings.")

        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PurpleSecondary, PurplePrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!uiState.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = uiState.avatarUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = listOfNotNull(profile?.firstName, profile?.lastName).joinToString(" ").ifBlank { "Unknown user" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = displayEmail ?: "No email",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Account",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "User ID", fontWeight = FontWeight.Medium)
                    Text(text = profile?.id?.take(8)?.plus("...") ?: "—", color = Color(0xFF6B7280))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Auth method", fontWeight = FontWeight.Medium)
                    Text(text = profile?.authMethod ?: "—", color = Color(0xFF6B7280))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Email", fontWeight = FontWeight.Medium)
                    Text(text = displayEmail ?: "No email", color = Color(0xFF6B7280))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Phone", fontWeight = FontWeight.Medium)
                    Text(text = profile?.phone ?: uiState.phone ?: "No phone", color = Color(0xFF6B7280))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlineButton(
            label = if (uiState.isLoading) "Signing out..." else "Log out",
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            viewModel.signOut {
                val intent = Intent(context, AuthActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(intent)
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = uiState.errorMessage ?: "",
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

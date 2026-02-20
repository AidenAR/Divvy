package com.example.divvy.ui.home.Views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.divvy.components.GroupIcon
import com.example.divvy.models.Group
import com.example.divvy.ui.home.ViewModels.HomeViewModel

private val GreenBg = Color(0xFFE8F5E9)
private val GreenText = Color(0xFF2E7D32)
private val RedBg = Color(0xFFFCE4EC)
private val RedText = Color(0xFFC62828)
private val Purple = Color(0xFF7C4DFF)
private val LightGray = Color(0xFFF5F5F5)

@Composable
fun HomeScreen(
    onNavigateToGroups: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your balances",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BalanceCard(
                        label = "You are owed",
                        amount = uiState.formattedOwed,
                        backgroundColor = GreenBg,
                        textColor = GreenText,
                        modifier = Modifier.weight(1f)
                    )
                    BalanceCard(
                        label = "You owe",
                        amount = uiState.formattedOwing,
                        backgroundColor = RedBg,
                        textColor = RedText,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                Text(
                    text = "GROUPS",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(uiState.groups) { group ->
                HomeGroupCard(group = group, onClick = { /** TODO: Navigate to Group Details */ })
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { /* TODO: create group */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "+ Create New Group",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        BottomActionBar(
            onScanReceipt = { /* TODO */ },
            onAddExpense = { /* TODO */ }
        )
    }
}

@Composable
private fun BalanceCard(
    label: String,
    amount: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun HomeGroupCard(
    group: Group,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group icon in a tinted circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Purple.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                GroupIcon(
                    iconName = group.iconName,
                    tint = Purple,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = group.balanceLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (group.isOwed) GreenText else RedText
                )
            }

            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Go to group",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun BottomActionBar(
    onScanReceipt: () -> Unit,
    onAddExpense: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scan Receipt — solid purple pill, flush left
        Button(
            onClick = onScanReceipt,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            modifier = Modifier.height(40.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Scan Receipt",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Add Expense — outlined style, flush right
        OutlinedButton(
            onClick = onAddExpense,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                text = "+ Add Expense",
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

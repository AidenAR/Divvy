package com.example.divvy.ui.groupdetail.Views

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.divvy.models.ActivityItem
import com.example.divvy.models.MemberBalance
import com.example.divvy.ui.groupdetail.ViewModels.GroupDetailViewModel

private val Purple = Color(0xFF7C4DFF)
private val Blue = Color(0xFF448AFF)
private val GreenBg = Color(0xFF2E7D32)
private val RedBg = Color(0xFFC62828)
private val GreenText = Color(0xFF2E7D32)
private val RedText = Color(0xFFC62828)
private val GreenBadgeBg = Color(0xFFE8F5E9)
private val RedBadgeBg = Color(0xFFFCE4EC)

private val avatarColors = listOf(
    Color(0xFF7C4DFF), // purple
    Color(0xFF2E7D32), // green
    Color(0xFFE65100), // orange
    Color(0xFF1565C0), // blue
    Color(0xFF00695C), // teal
)

@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
) {
    val viewModel: GroupDetailViewModel = hiltViewModel<GroupDetailViewModel, GroupDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(groupId) }
    )
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = uiState.group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
//                IconButton(onClick = { /* TODO: history */ }) {
//                    Icon(
//                        imageVector = Icons.Filled.History,
//                        contentDescription = "History"
//                    )
//                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
            ) {
                // Balance summary card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    BalanceSummaryCard(balanceCents = uiState.group.balanceCents)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // BALANCES section
                item {
                    SectionLabel("BALANCES")
                    Spacer(modifier = Modifier.height(10.dp))
                }
                items(uiState.memberBalances) { mb ->
                    MemberBalanceCard(
                        memberBalance = mb,
                        avatarColor = avatarColors[uiState.memberBalances.indexOf(mb) % avatarColors.size]
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ACTIVITY section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionLabel("ACTIVITY")
                    Spacer(modifier = Modifier.height(10.dp))
                }
                items(uiState.activity) { item ->
                    ActivityCard(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Fixed "Add New Expense" gradient button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Brush.horizontalGradient(listOf(Purple, Blue)))
                .clickable { /* TODO */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add New Expense",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun BalanceSummaryCard(balanceCents: Long) {
    val isOwed = balanceCents >= 0
    val bgColor = if (isOwed) GreenBg else RedBg
    val label = if (isOwed) "You are owed" else "You owe"
    val dollars = kotlin.math.abs(balanceCents) / 100.0
    val amount = "$${String.format("%.2f", dollars)}"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Color.Gray,
        letterSpacing = 1.sp
    )
}

@Composable
private fun MemberBalanceCard(memberBalance: MemberBalance, avatarColor: Color) {
    val isOwedByThem = memberBalance.balanceCents >= 0
    val arrowLabel = if (isOwedByThem) "↑ owes you" else "↓ you owe"
    val amountColor = if (isOwedByThem) GreenText else RedText
    val dollars = kotlin.math.abs(memberBalance.balanceCents) / 100.0
    val amount = "$${String.format("%.2f", dollars)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Letter avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = memberBalance.name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = memberBalance.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$arrowLabel $amount",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        }
    }
}

@Composable
private fun ActivityCard(item: ActivityItem) {
    val dollars = item.amountCents / 100.0
    val amount = "$${String.format("%.2f", dollars)}"
    val badgeLabel = if (item.paidByCurrentUser) "You paid" else "You owe"
    val badgeBg = if (item.paidByCurrentUser) GreenBadgeBg else RedBadgeBg
    val badgeTextColor = if (item.paidByCurrentUser) GreenText else RedText

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.dateLabel} · Paid by ${item.paidByLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

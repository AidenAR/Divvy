package com.example.divvy.ui.assignitems.Views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.divvy.models.SupportedCurrency
import com.example.divvy.models.formatAmount
import com.example.divvy.ui.assignitems.ViewModels.AssignItemsViewModel
import com.example.divvy.ui.assignitems.ViewModels.AssignMember
import com.example.divvy.ui.assignitems.ViewModels.ReceiptItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignItemsScreen(
    viewModel: AssignItemsViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.done.collect { onDone() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Assign Items",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(enabled = !uiState.isSaving, onClick = viewModel::onNext)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (uiState.isSaving) "..." else "Done",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                StoreInfoCard(
                    description = uiState.description,
                    amount = uiState.amountDisplay,
                    currencyCode = uiState.currency
                )
                Spacer(Modifier.height(18.dp))
            }

            item {
                MemberChipsRow(members = uiState.members)
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "TAP ITEMS TO ASSIGN",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(14.dp))
            }

            items(uiState.items, key = { it.id }) { item ->
                val isExpanded = uiState.expandedItemId == item.id
                val assignedIds = uiState.assignments[item.id].orEmpty()
                val assignedMembers = uiState.members.filter { it.id in assignedIds }

                ItemCard(
                    item = item,
                    currencyCode = uiState.currency,
                    isExpanded = isExpanded,
                    assignedMembers = assignedMembers,
                    allMembers = uiState.members,
                    assignedMemberIds = assignedIds,
                    onTap = { viewModel.onItemTap(item.id) },
                    onToggleMember = { memberId ->
                        viewModel.onToggleMemberForItem(item.id, memberId)
                    }
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "COVERING",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Gift someone's share so they don't owe",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
            }

            items(uiState.members, key = { "cover_${it.id}" }) { member ->
                CoveringMemberRow(
                    member = member,
                    covererName = uiState.coveredBy[member.id]?.let { coverId ->
                        uiState.members.firstOrNull { it.id == coverId }?.name
                    },
                    isExpanded = uiState.expandedCoveringMemberId == member.id,
                    allMembers = uiState.members,
                    onToggleCovering = { viewModel.onToggleCoveringForMember(member.id) },
                    onSetCovering = { covererUserId ->
                        viewModel.onSetCovering(member.id, covererUserId)
                    },
                )
                Spacer(Modifier.height(8.dp))
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StoreInfoCard(description: String, amount: String, currencyCode: String = "USD") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        val displayAmount = amount.toDoubleOrNull()?.let {
            formatAmount((it * 100).toLong(), currencyCode)
        } ?: "${SupportedCurrency.fromCode(currencyCode).symbol}$amount"
        Text(
            text = displayAmount,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun MemberChipsRow(members: List<AssignMember>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        members.forEach { member ->
            MemberChip(member = member)
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun MemberChip(member: AssignMember) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(member.color)
            .padding(start = 10.dp, end = 14.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = member.name,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemCard(
    item: ReceiptItem,
    currencyCode: String = "USD",
    isExpanded: Boolean,
    assignedMembers: List<AssignMember>,
    allMembers: List<AssignMember>,
    assignedMemberIds: Set<String>,
    onTap: () -> Unit,
    onToggleMember: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                if (assignedMembers.isEmpty()) {
                    Text(
                        text = "Not assigned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-4).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        assignedMembers.forEach { member ->
                            InitialAvatar(
                                name = member.name,
                                color = member.color,
                                size = 24
                            )
                        }
                    }
                }
            }

            Text(
                text = item.formattedPrice(currencyCode),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                allMembers.forEach { member ->
                    val isAssigned = member.id in assignedMemberIds
                    MemberAssignChip(
                        member = member,
                        isAssigned = isAssigned,
                        onClick = { onToggleMember(member.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InitialAvatar(
    name: String,
    color: Color,
    size: Int = 24
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.first().uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size / 2).sp
        )
    }
}

@Composable
private fun MemberAssignChip(
    member: AssignMember,
    isAssigned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isAssigned) member.color else MaterialTheme.colorScheme.surface
    val borderColor = if (isAssigned) member.color else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = member.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isAssigned) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isAssigned) Color.White else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun CoveringMemberRow(
    member: AssignMember,
    covererName: String?,
    isExpanded: Boolean,
    allMembers: List<AssignMember>,
    onToggleCovering: () -> Unit,
    onSetCovering: (covererUserId: String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(member.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.first().uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (covererName != null) {
                    Text(
                        text = "Covered by $covererName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (covererName != null) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Remove covering",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable { onSetCovering(null) }
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.CardGiftcard,
                    contentDescription = "Cover this share",
                    tint = if (isExpanded) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onToggleCovering)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(start = 60.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Who's covering ${member.name}'s share?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allMembers.filter { it.id != member.id }.forEach { candidate ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onSetCovering(candidate.id) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = candidate.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
        }
    }
}

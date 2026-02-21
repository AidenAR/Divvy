package com.example.divvy.ui.splitexpense.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.divvy.components.GroupIcon
import com.example.divvy.models.Group
import com.example.divvy.ui.splitexpense.ViewModels.SplitExpenseViewModel
import com.example.divvy.ui.splitexpense.ViewModels.SplitMethod

private val Purple = Color(0xFF7C4DFF)
private val Blue = Color(0xFF448AFF)
private val LightGray = Color(0xFFF5F5F5)
private val BorderGray = Color(0xFFE8E8E8)
private val TextGray = Color(0xFF999999)
private val SubtitleGray = Color(0xFF888888)

private val GradientBrush = Brush.horizontalGradient(listOf(Purple, Blue))

@Composable
fun SplitExpenseScreen(
    viewModel: SplitExpenseViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToAssignItems: (groupId: String, amount: String, description: String) -> Unit = { _, _, _ -> },
    onNavigateToSplitByPercentage: (groupId: String, amount: String, description: String) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SplitExpenseViewModel.SplitEvent.Created -> onBack()
                is SplitExpenseViewModel.SplitEvent.GoToAssignItems ->
                    onNavigateToAssignItems(event.groupId, event.amount, event.description)
                is SplitExpenseViewModel.SplitEvent.GoToSplitByPercentage ->
                    onNavigateToSplitByPercentage(event.groupId, event.amount, event.description)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            AmountSection(
                amount = uiState.amount,
                onAmountChange = viewModel::onAmountChange
            )

            Spacer(Modifier.height(16.dp))

            DescriptionField(
                description = uiState.description,
                onDescriptionChange = viewModel::onDescriptionChange
            )

            Spacer(Modifier.height(28.dp))

            GroupSelectionSection(
                groups = uiState.groups,
                selectedGroupId = uiState.selectedGroupId,
                onGroupSelected = viewModel::onGroupSelected
            )

            Spacer(Modifier.height(28.dp))

            SplitMethodSection(
                selectedMethod = uiState.splitMethod,
                onMethodSelected = viewModel::onSplitMethodSelected
            )

            Spacer(Modifier.height(32.dp))
        }

        CreateSplitButton(
            enabled = uiState.amount.isNotBlank() && uiState.selectedGroupId != null,
            isCreating = uiState.isCreating,
            onClick = viewModel::onCreateSplit
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Split Expense",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun AmountSection(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    Text(
        text = "Total Amount",
        style = MaterialTheme.typography.bodySmall,
        color = TextGray,
        fontSize = 13.sp
    )

    Spacer(Modifier.height(8.dp))

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Spacer(Modifier.width(4.dp))

        BasicTextField(
            value = amount,
            onValueChange = onAmountChange,
            textStyle = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            cursorBrush = SolidColor(Purple),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (amount.isEmpty()) {
                    Text(
                        text = "0.00",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun DescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    BasicTextField(
        value = description,
        onValueChange = onDescriptionChange,
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        cursorBrush = SolidColor(Purple),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGray, RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (description.isEmpty()) {
                    Text(
                        text = "What's this for?",
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun GroupSelectionSection(
    groups: List<Group>,
    selectedGroupId: String?,
    onGroupSelected: (String) -> Unit
) {
    Text(
        text = "Select Group",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        fontSize = 15.sp
    )

    Spacer(Modifier.height(14.dp))

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        groups.forEachIndexed { index, group ->
            val isSelected = group.id == selectedGroupId
            if (isSelected) {
                SelectedGroupItem(group = group, onClick = { onGroupSelected(group.id) })
            } else {
                UnselectedGroupItem(group = group, onClick = { onGroupSelected(group.id) })
                if (index < groups.lastIndex && groups[index + 1].id != selectedGroupId) {
                    HorizontalDivider(
                        color = LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedGroupItem(group: Group, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GradientBrush)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${group.name} ${group.icon.toEmoji()}",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun UnselectedGroupItem(group: Group, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            text = "${group.name} ${group.icon.toEmoji()}",
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun SplitMethodSection(
    selectedMethod: SplitMethod,
    onMethodSelected: (SplitMethod) -> Unit
) {
    Text(
        text = "Split Method",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        fontSize = 15.sp
    )

    Spacer(Modifier.height(14.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SplitMethod.entries.forEach { method ->
            SplitMethodCard(
                method = method,
                isSelected = method == selectedMethod,
                onClick = { onMethodSelected(method) }
            )
        }
    }
}

@Composable
private fun SplitMethodCard(
    method: SplitMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Purple else BorderGray
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) GradientBrush
                    else Brush.horizontalGradient(listOf(LightGray, LightGray))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = method.icon(),
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = method.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.Black
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = method.subtitle,
                fontSize = 12.sp,
                color = SubtitleGray
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = Purple,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CreateSplitButton(
    enabled: Boolean,
    isCreating: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp, top = 8.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(
                if (enabled && !isCreating) GradientBrush
                else Brush.horizontalGradient(listOf(Color.LightGray, Color.LightGray))
            )
            .clickable(enabled = enabled && !isCreating, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isCreating) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Create Split",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

private fun SplitMethod.icon(): ImageVector = when (this) {
    SplitMethod.Equally -> Icons.Rounded.AttachMoney
    SplitMethod.ByPercentage -> Icons.Rounded.Percent
    SplitMethod.ByItems -> Icons.Rounded.Checklist
}

private fun GroupIcon.toEmoji(): String = when (this) {
    GroupIcon.Home -> "\uD83C\uDFE0"
    GroupIcon.Flight -> "\u2708\uFE0F"
    GroupIcon.Restaurant -> "\uD83C\uDF71"
    GroupIcon.Work -> "\uD83D\uDCBC"
    GroupIcon.ShoppingBag -> "\uD83D\uDECD\uFE0F"
    GroupIcon.Grocery -> "\uD83D\uDED2"
    GroupIcon.Car -> "\uD83D\uDE97"
    GroupIcon.School -> "\uD83C\uDFEB"
    GroupIcon.Movie -> "\uD83C\uDFAC"
    GroupIcon.Music -> "\uD83C\uDFB5"
    GroupIcon.Pets -> "\uD83D\uDC3E"
    GroupIcon.Celebration -> "\uD83C\uDF89"
    GroupIcon.Gaming -> "\uD83C\uDFAE"
    GroupIcon.Bank -> "\uD83C\uDFE6"
    GroupIcon.Group -> "\uD83D\uDC65"
}

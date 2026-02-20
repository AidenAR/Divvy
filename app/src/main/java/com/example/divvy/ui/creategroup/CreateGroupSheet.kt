package com.example.divvy.ui.creategroup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.divvy.components.GroupIcon
import com.example.divvy.components.GroupIcon as GroupIconComposable

private val Purple = Color(0xFF7C4DFF)
private val Blue = Color(0xFF448AFF)
private val LightGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupSheet(
    name: String,
    selectedIcon: GroupIcon,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onIconSelected: (GroupIcon) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: title
            Text(
                text = "Create New Group",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Group Name label + field
            Text(
                text = "Group Name",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("e.g., Summer Vacation", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LightGray,
                    unfocusedContainerColor = LightGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Icon picker
            Text(
                text = "Choose an Icon",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(GroupIcon.entries) { icon ->
                    val isSelected = icon == selectedIcon
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Purple.copy(alpha = 0.12f) else LightGray
                            )
                            .then(
                                if (isSelected)
                                    Modifier.border(2.dp, Purple, RoundedCornerShape(12.dp))
                                else
                                    Modifier
                            )
                            .clickable { onIconSelected(icon) },
                        contentAlignment = Alignment.Center
                    ) {
                        GroupIconComposable(
                            icon = icon,
                            tint = if (isSelected) Purple else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))

            // Gradient button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        brush = if (name.isNotBlank() && !isLoading)
                            Brush.horizontalGradient(listOf(Purple, Blue))
                        else
                            Brush.horizontalGradient(listOf(Color.LightGray, Color.LightGray))
                    )
                    .clickable(enabled = name.isNotBlank() && !isLoading) { onCreate() },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create Group",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview_CreateGroupSheet() {
    CreateGroupSheet(
        name = "Test",
        selectedIcon = GroupIcon.Home,
        isLoading = false,
        onNameChange = {},
        onIconSelected = {},
        onCreate = {},
        onDismiss = {},
    )
}

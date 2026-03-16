package com.example.divvy.ui.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.divvy.components.GroupIcon
import com.example.divvy.models.Group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToGroupSheet(
    availableGroups: List<Group>,
    selectedGroupId: String?,
    members: List<ContactOnDivvy>,
    selectedMemberIds: Set<String>,
    memberSearchQuery: String,
    onGroupSelected: (String) -> Unit,
    onMemberSearchChange: (String) -> Unit,
    onToggleMember: (String) -> Unit,
    onConfirm: () -> Unit,
    onCreateNewGroup: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            if (selectedGroupId == null) {
                // Step 1: Pick a group
                Text(
                    text = "Add to Group",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(16.dp))

                if (availableGroups.isEmpty()) {
                    Text(
                        text = "You don't have any groups yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(availableGroups, key = { it.id }) { group ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onGroupSelected(group.id) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.example.divvy.components.GroupIcon(
                                    icon = group.icon,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreateNewGroup() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Create New Group",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Step 2: Pick members
                val groupName = availableGroups.find { it.id == selectedGroupId }?.name ?: "Group"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onGroupSelected("") }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Add to $groupName",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))

                MemberPickerList(
                    members = members,
                    selectedMemberIds = selectedMemberIds,
                    searchQuery = memberSearchQuery,
                    onSearchChange = onMemberSearchChange,
                    onToggleMember = onToggleMember
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onConfirm,
                    enabled = selectedMemberIds.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add ${if (selectedMemberIds.size == 1) "1 Member" else "${selectedMemberIds.size} Members"}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupSheet(
    name: String,
    selectedIcon: GroupIcon,
    isCreating: Boolean,
    error: String?,
    members: List<ContactOnDivvy>,
    selectedMemberIds: Set<String>,
    memberSearchQuery: String,
    onNameChange: (String) -> Unit,
    onIconSelected: (GroupIcon) -> Unit,
    onMemberSearchChange: (String) -> Unit,
    onToggleMember: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Create Group",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Icon",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            // Icon grid
            val icons = GroupIcon.entries
            val chunked = icons.chunked(5)
            chunked.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { icon ->
                        val isSelected = icon == selectedIcon
                        val tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                        IconButton(onClick = { onIconSelected(icon) }) {
                            com.example.divvy.components.GroupIcon(
                                icon = icon,
                                modifier = Modifier.size(28.dp),
                                tint = tint
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Members",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            MemberPickerList(
                members = members,
                selectedMemberIds = selectedMemberIds,
                searchQuery = memberSearchQuery,
                onSearchChange = onMemberSearchChange,
                onToggleMember = onToggleMember
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onSubmit,
                enabled = name.isNotBlank() && !isCreating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create & Add Members")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsAddToGroupSheet(
    availableGroups: List<Group>,
    onGroupSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Add to Group",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(16.dp))

            if (availableGroups.isEmpty()) {
                Text(
                    text = "You don't have any groups yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(availableGroups, key = { it.id }) { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGroupSelected(group.id) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.example.divvy.components.GroupIcon(
                                icon = group.icon,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsCreateGroupSheet(
    name: String,
    selectedIcon: GroupIcon,
    isCreating: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onIconSelected: (GroupIcon) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Create Group",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Icon",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            val icons = GroupIcon.entries
            val chunked = icons.chunked(5)
            chunked.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { icon ->
                        val isSelected = icon == selectedIcon
                        val tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                        IconButton(onClick = { onIconSelected(icon) }) {
                            com.example.divvy.components.GroupIcon(
                                icon = icon,
                                modifier = Modifier.size(28.dp),
                                tint = tint
                            )
                        }
                    }
                }
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onSubmit,
                enabled = name.isNotBlank() && !isCreating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create")
                }
            }
        }
    }
}

@Composable
fun MemberPickerList(
    members: List<ContactOnDivvy>,
    selectedMemberIds: Set<String>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleMember: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Search contacts...") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )

    Spacer(Modifier.height(8.dp))

    LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
        items(members, key = { it.profile.id }) { contact ->
            val profile = contact.profile
            val name = "${profile.firstName} ${profile.lastName}".trim()
            val initials = profile.firstName.take(1) + profile.lastName.take(1)
            ContactRow(
                name = name,
                subtitle = profile.phone ?: profile.email,
                initials = initials,
                isSelected = profile.id in selectedMemberIds,
                onClick = { onToggleMember(profile.id) },
                groupBadges = contact.sharedGroups.map { it.name }
            )
        }
    }
}

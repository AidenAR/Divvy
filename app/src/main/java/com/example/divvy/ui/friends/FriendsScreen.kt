package com.example.divvy.ui.friends

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.divvy.models.ContactEntry

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = hiltViewModel(),
    onCreatedGroupNavigate: (String) -> Unit = {},
    onAddExpenseNavigate: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle navigation after group creation
    LaunchedEffect(uiState.createdGroupId) {
        uiState.createdGroupId?.let { groupId ->
            onCreatedGroupNavigate(groupId)
            viewModel.onCreatedGroupNavigationHandled()
        }
    }

    // Handle navigation for 1-on-1 expense
    LaunchedEffect(uiState.navigateToSplitWithGroupId) {
        uiState.navigateToSplitWithGroupId?.let { groupId ->
            onAddExpenseNavigate(groupId)
            viewModel.onNavigateToSplitHandled()
        }
    }

    // Contacts permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CONTACTS] == true
        val writeGranted = permissions[Manifest.permission.WRITE_CONTACTS] == true
        if (readGranted) viewModel.onContactsPermissionGranted()
        if (writeGranted) viewModel.onWriteContactsPermissionGranted()
    }

    LaunchedEffect(Unit) {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasReadPermission) viewModel.onContactsPermissionGranted()
        if (hasWritePermission) viewModel.onWriteContactsPermissionGranted()
        if (!hasReadPermission || !hasWritePermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        }
    }

    val filteredFriends = viewModel.filteredFriends()
    val filteredDeviceContacts = viewModel.filteredDeviceContacts()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchChange(it) },
                placeholder = { Text("Search contacts...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Action bar when items selected
            if (uiState.selectedKeys.isNotEmpty()) {
                SelectionActionBar(
                    count = uiState.selectedKeys.size,
                    onAddToGroup = { viewModel.onShowAddToGroupSheet() },
                    onCreateGroup = { viewModel.onShowCreateGroupSheet() },
                    onAddExpense = if (uiState.selectedKeys.size == 1) {
                        { viewModel.onAddExpenseWithSelectedFriend() }
                    } else null,
                    onClear = { viewModel.onClearSelection() }
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Friends section
                if (filteredFriends.isNotEmpty()) {
                    item {
                        SectionHeader("Friends")
                    }
                    items(filteredFriends, key = { it.selectionKey }) { friend ->
                        ContactRow(
                            name = friend.displayName,
                            subtitle = null,
                            initials = friend.profile.firstName.take(1) + friend.profile.lastName.take(1),
                            isSelected = friend.selectionKey in uiState.selectedKeys,
                            onClick = {
                                if (uiState.selectedKeys.isNotEmpty()) {
                                    viewModel.onToggleSelection(friend.selectionKey)
                                }
                            },
                            onLongClick = { viewModel.onToggleSelection(friend.selectionKey) },
                            groupBadges = friend.sharedGroups.map { it.name }
                        )
                    }
                }

                // Device contacts section
                if (filteredDeviceContacts.isNotEmpty()) {
                    item {
                        SectionHeader("Not on Divvy")
                    }
                    items(filteredDeviceContacts, key = { it.selectionKey }) { contact ->
                        ContactRow(
                            name = contact.name,
                            subtitle = contact.contactValue,
                            initials = contact.name.take(1).uppercase(),
                            isSelected = false,
                            onClick = null,
                            trailingContent = {
                                OutlinedButton(onClick = { viewModel.onInviteContact(context, contact) }) {
                                    Text("Invite")
                                }
                            }
                        )
                    }
                }

                // Empty state
                if (filteredFriends.isEmpty() && filteredDeviceContacts.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) "No results found"
                                else "No friends yet. Join a group to see friends here!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Bottom sheets
    if (uiState.showAddContactSheet) {
        AddContactSheet(
            name = uiState.addContactName,
            phone = uiState.addContactPhone,
            email = uiState.addContactEmail,
            matchedProfile = uiState.addContactMatchedProfile,
            isAdding = uiState.isAddingContact,
            onNameChange = viewModel::onAddContactNameChange,
            onPhoneChange = viewModel::onAddContactPhoneChange,
            onEmailChange = viewModel::onAddContactEmailChange,
            onSave = viewModel::onAddContactSubmit,
            onDismiss = viewModel::onDismissAddContactSheet
        )
    }

    if (uiState.showActionSheet) {
        FriendsAddToGroupSheet(
            availableGroups = uiState.availableGroups,
            onGroupSelected = viewModel::onAddSelectedFriendsToGroup,
            onDismiss = viewModel::onDismissActionSheet
        )
    }

    if (uiState.showCreateGroupSheet) {
        FriendsCreateGroupSheet(
            name = uiState.createGroupName,
            selectedIcon = uiState.createGroupIcon,
            isCreating = uiState.isCreatingGroup,
            error = uiState.createGroupError,
            onNameChange = viewModel::onCreateGroupNameChange,
            onIconSelected = viewModel::onCreateGroupIconSelected,
            onSubmit = viewModel::submitCreateGroup,
            onDismiss = viewModel::onDismissCreateGroupSheet
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SelectionActionBar(
    count: Int,
    onAddToGroup: () -> Unit,
    onCreateGroup: () -> Unit,
    onAddExpense: (() -> Unit)? = null,
    onClear: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$count selected",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onClear() }
            )
            Spacer(Modifier.width(12.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onAddExpense != null) {
                    OutlinedButton(
                        onClick = onAddExpense,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Add Expense", fontSize = 12.sp, maxLines = 1)
                    }
                }
                OutlinedButton(
                    onClick = onAddToGroup,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Add to Group", fontSize = 12.sp, maxLines = 1)
                }
                Button(
                    onClick = onCreateGroup,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("New Group", fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ContactRow(
    name: String,
    subtitle: String?,
    initials: String,
    isSelected: Boolean,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)? = null,
    groupBadges: List<String> = emptyList(),
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                when {
                    onLongClick != null -> Modifier.combinedClickable(
                        onLongClick = onLongClick,
                        onClick = { onClick?.invoke() }
                    )
                    onClick != null -> Modifier.clickable { onClick() }
                    else -> Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        val avatarColor = MaterialTheme.colorScheme.primaryContainer
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.width(12.dp))

        // Name + subtitle + group badges
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (groupBadges.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    groupBadges.forEach { groupName ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = groupName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Trailing: selection check or custom content
        if (trailingContent != null) {
            trailingContent()
        } else if (isSelected) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

package com.example.divvy.ui.friends

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.AuthRepository
import com.example.divvy.backend.ContactsRepository
import com.example.divvy.backend.FriendsRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.backend.MemberRepository
import com.example.divvy.backend.DataResult
import com.example.divvy.components.GroupIcon
import com.example.divvy.models.ContactEntry
import com.example.divvy.models.ContactType
import com.example.divvy.models.Group
import com.example.divvy.models.ProfileRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactOnDivvy(
    val profile: ProfileRow,
    val sharedGroups: List<Group> = emptyList()
)

data class FriendsUiState(
    val friends: List<ContactEntry.DivvyFriend> = emptyList(),
    val deviceContacts: List<ContactEntry.DeviceContact> = emptyList(),
    val searchQuery: String = "",
    val selectedKeys: Set<String> = emptySet(),
    val hasContactsPermission: Boolean = false,
    val hasWriteContactsPermission: Boolean = false,
    val isLoading: Boolean = false,
    // Add contact sheet
    val showAddContactSheet: Boolean = false,
    val addContactName: String = "",
    val addContactPhone: String = "",
    val addContactEmail: String = "",
    val addContactMatchedProfile: ProfileRow? = null,
    val isAddingContact: Boolean = false,
    // Action sheet (add to group)
    val showActionSheet: Boolean = false,
    val actionSheetGroupId: String? = null,
    val availableGroups: List<Group> = emptyList(),
    // Create group sheet
    val showCreateGroupSheet: Boolean = false,
    val createGroupName: String = "",
    val createGroupIcon: GroupIcon = GroupIcon.Group,
    val isCreatingGroup: Boolean = false,
    val createGroupError: String? = null,
    val createdGroupId: String? = null,
    // Navigation for 1-on-1 expense
    val navigateToSplitWithGroupId: String? = null,
    // Member picker
    val memberSearchQuery: String = "",
    val selectedMemberIds: Set<String> = emptySet(),
    val contactsOnDivvy: List<ContactOnDivvy> = emptyList(),
    val isLoadingProfiles: Boolean = false,
    // All user groups (for computing available groups)
    val allGroups: List<Group> = emptyList()
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository,
    private val contactsRepository: ContactsRepository,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState(isLoading = true))
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()
    private val currentUserId: String = authRepository.getCurrentUserId()

    init {
        loadFriends()
        loadGroups()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                friendsRepository.getFriendsWithGroups()
            }.onSuccess { friendsWithGroups ->
                val friends = friendsWithGroups.map { fwg ->
                    ContactEntry.DivvyFriend(profile = fwg.profile, sharedGroups = fwg.sharedGroups)
                }.sortedBy { it.displayName.lowercase() }
                _uiState.update { it.copy(friends = friends, isLoading = false) }
                if (_uiState.value.hasContactsPermission) {
                    loadDeviceContacts()
                    mergeContactsOnDivvy()
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            groupRepository.listGroups().collect { result ->
                if (result is DataResult.Success) {
                    _uiState.update { it.copy(allGroups = result.data) }
                }
            }
        }
    }

    fun onContactsPermissionGranted() {
        _uiState.update { it.copy(hasContactsPermission = true) }
        loadDeviceContacts()
        mergeContactsOnDivvy()
    }

    fun onWriteContactsPermissionGranted() {
        _uiState.update { it.copy(hasWriteContactsPermission = true) }
    }

    private fun loadDeviceContacts() {
        viewModelScope.launch {
            runCatching {
                contactsRepository.getDeviceContacts()
            }.onSuccess { rawContacts ->
                val friendPhones = _uiState.value.friends
                    .mapNotNull { it.profile.phone?.normalizePhone() }
                    .toSet()
                val friendEmails = _uiState.value.friends
                    .mapNotNull { it.profile.email?.lowercase() }
                    .toSet()

                val deviceEntries = rawContacts.flatMap { raw ->
                    val phoneEntries = raw.phones.mapNotNull { phone ->
                        val normalized = phone.normalizePhone()
                        if (normalized in friendPhones) null
                        else ContactEntry.DeviceContact(raw.name, phone, ContactType.PHONE)
                    }
                    val emailEntries = raw.emails.mapNotNull { email ->
                        if (email.lowercase() in friendEmails) null
                        else ContactEntry.DeviceContact(raw.name, email, ContactType.EMAIL)
                    }
                    phoneEntries + emailEntries
                }
                _uiState.update { it.copy(deviceContacts = deviceEntries.sortedBy { c -> c.displayName.lowercase() }) }
            }
        }
    }

    private fun mergeContactsOnDivvy() {
        viewModelScope.launch {
            runCatching {
                val rawContacts = contactsRepository.getDeviceContacts()
                val allPhones = rawContacts.flatMap { it.phones }.map { it.normalizePhone() }.filter { it.isNotBlank() }.distinct()
                val allEmails = rawContacts.flatMap { it.emails }.map { it.lowercase() }.filter { it.isNotBlank() }.distinct()
                val phoneProfiles = friendsRepository.getProfilesByPhones(allPhones)
                val emailProfiles = friendsRepository.getProfilesByEmails(allEmails)
                (phoneProfiles + emailProfiles).distinctBy { it.id }.filter { it.id != currentUserId }
            }.onSuccess { contactProfiles ->
                _uiState.update { state ->
                    val existingIds = state.friends.map { it.profile.id }.toSet()
                    val newFriends = contactProfiles
                        .filter { it.id !in existingIds }
                        .map { ContactEntry.DivvyFriend(profile = it, sharedGroups = emptyList()) }
                    val merged = (state.friends + newFriends).sortedBy { it.displayName.lowercase() }
                    state.copy(friends = merged)
                }
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onToggleSelection(key: String) {
        _uiState.update { current ->
            val next = current.selectedKeys.toMutableSet()
            if (next.contains(key)) next.remove(key) else next.add(key)
            current.copy(selectedKeys = next)
        }
    }

    fun onClearSelection() {
        _uiState.update { it.copy(selectedKeys = emptySet()) }
    }

    fun onShowAddContactSheet() {
        _uiState.update {
            it.copy(
                showAddContactSheet = true,
                addContactName = "",
                addContactPhone = "",
                addContactEmail = "",
                addContactMatchedProfile = null,
                isAddingContact = false
            )
        }
    }

    fun onDismissAddContactSheet() {
        _uiState.update { it.copy(showAddContactSheet = false) }
    }

    fun onAddContactNameChange(value: String) { _uiState.update { it.copy(addContactName = value) } }
    fun onAddContactPhoneChange(value: String) { _uiState.update { it.copy(addContactPhone = value) } }
    fun onAddContactEmailChange(value: String) { _uiState.update { it.copy(addContactEmail = value) } }

    fun onAddContactSubmit() {
        viewModelScope.launch {
            val state = _uiState.value
            val name = state.addContactName.trim()
            if (name.isBlank()) return@launch
            _uiState.update { it.copy(isAddingContact = true) }
            if (state.hasWriteContactsPermission) {
                runCatching {
                    contactsRepository.addDeviceContact(
                        name = name,
                        phone = state.addContactPhone.takeIf { it.isNotBlank() },
                        email = state.addContactEmail.takeIf { it.isNotBlank() }
                    )
                }
            }
            var matchedProfile: ProfileRow? = null
            if (state.addContactPhone.isNotBlank()) {
                matchedProfile = runCatching { friendsRepository.getProfileByPhone(state.addContactPhone.trim()) }.getOrNull()
            }
            if (matchedProfile == null && state.addContactEmail.isNotBlank()) {
                matchedProfile = runCatching { friendsRepository.getProfileByEmail(state.addContactEmail.trim()) }.getOrNull()
            }
            _uiState.update { it.copy(isAddingContact = false, addContactMatchedProfile = matchedProfile) }
            if (_uiState.value.hasContactsPermission) loadDeviceContacts()
            if (matchedProfile == null) _uiState.update { it.copy(showAddContactSheet = false) }
        }
    }

    fun onInviteContact(context: Context, contact: ContactEntry.DeviceContact) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Join me on Divvy! Split expenses easily with friends. Download now!")
        }
        context.startActivity(Intent.createChooser(sendIntent, "Invite to Divvy"))
    }

    fun onShowAddToGroupSheet() {
        _uiState.update { it.copy(showActionSheet = true, availableGroups = it.allGroups) }
    }

    fun onDismissActionSheet() {
        _uiState.update { it.copy(showActionSheet = false, actionSheetGroupId = null) }
    }

    fun onAddSelectedFriendsToGroup(groupId: String) {
        viewModelScope.launch {
            val userIds = _uiState.value.selectedKeys
                .filter { it.startsWith("divvy_") }
                .map { it.removePrefix("divvy_") }
            userIds.forEach { userId -> runCatching { memberRepository.addMember(groupId, userId) } }
            _uiState.update { it.copy(showActionSheet = false, selectedKeys = emptySet()) }
            loadFriends()
        }
    }

    fun onShowCreateGroupSheet() {
        _uiState.update {
            it.copy(
                showCreateGroupSheet = true,
                showActionSheet = false,
                createGroupName = "",
                createGroupIcon = GroupIcon.Group,
                createGroupError = null,
                isCreatingGroup = false
            )
        }
    }

    fun onDismissCreateGroupSheet() {
        _uiState.update { it.copy(showCreateGroupSheet = false) }
    }

    fun onCreateGroupNameChange(value: String) { _uiState.update { it.copy(createGroupName = value) } }
    fun onCreateGroupIconSelected(icon: GroupIcon) { _uiState.update { it.copy(createGroupIcon = icon) } }

    fun submitCreateGroup() {
        viewModelScope.launch {
            val state = _uiState.value
            val name = state.createGroupName.trim()
            if (name.isBlank()) {
                _uiState.update { it.copy(createGroupError = "Group name is required.") }
                return@launch
            }
            _uiState.update { it.copy(isCreatingGroup = true, createGroupError = null) }
            val userIds = state.selectedKeys
                .filter { it.startsWith("divvy_") }
                .map { it.removePrefix("divvy_") }
            runCatching {
                val group = groupRepository.createGroup(name, state.createGroupIcon)
                userIds.forEach { userId -> runCatching { memberRepository.addMember(group.id, userId) } }
                groupRepository.refreshGroups()
                group
            }.onSuccess { group ->
                _uiState.update { it.copy(isCreatingGroup = false, showCreateGroupSheet = false, selectedKeys = emptySet(), createdGroupId = group.id) }
                loadFriends()
            }.onFailure {
                _uiState.update { it.copy(isCreatingGroup = false, createGroupError = "Unable to create group.") }
            }
        }
    }

    fun onAddExpenseWithSelectedFriend() {
        val selectedKey = _uiState.value.selectedKeys.firstOrNull() ?: return
        if (!selectedKey.startsWith("divvy_")) return
        val friendId = selectedKey.removePrefix("divvy_")
        val friend = _uiState.value.friends.find { it.profile.id == friendId } ?: return

        viewModelScope.launch {
            // Check for existing 1-on-1 group
            val existing1on1 = friend.sharedGroups.find { group ->
                runCatching {
                    memberRepository.refreshMembers(group.id)
                    val members = memberRepository.getMembers(group.id).first()
                    members.size == 1 // Only 1 other person besides current user
                }.getOrDefault(false)
            }

            if (existing1on1 != null) {
                _uiState.update { it.copy(navigateToSplitWithGroupId = existing1on1.id, selectedKeys = emptySet()) }
            } else {
                // Create a new 1-on-1 group automatically
                runCatching {
                    val groupName = "${friend.profile.firstName} and You"
                    val group = groupRepository.createGroup(groupName, GroupIcon.Group)
                    memberRepository.addMember(group.id, friendId)
                    groupRepository.refreshGroups()
                    group
                }.onSuccess { group ->
                    _uiState.update { it.copy(navigateToSplitWithGroupId = group.id, selectedKeys = emptySet()) }
                    loadFriends()
                }
            }
        }
    }

    fun onNavigateToSplitHandled() {
        _uiState.update { it.copy(navigateToSplitWithGroupId = null) }
    }

    fun onCreatedGroupNavigationHandled() {
        _uiState.update { it.copy(createdGroupId = null) }
    }

    fun filteredFriends(): List<ContactEntry.DivvyFriend> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isBlank()) return _uiState.value.friends
        return _uiState.value.friends.filter { it.displayName.lowercase().contains(query) }
    }

    fun filteredDeviceContacts(): List<ContactEntry.DeviceContact> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isBlank()) return _uiState.value.deviceContacts
        return _uiState.value.deviceContacts.filter {
            it.name.lowercase().contains(query) || it.contactValue.lowercase().contains(query)
        }
    }

    private fun String.normalizePhone(): String = replace(Regex("[^\\d]"), "")
}

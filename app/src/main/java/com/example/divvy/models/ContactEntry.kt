package com.example.divvy.models

sealed interface ContactEntry {
    val displayName: String
    val selectionKey: String

    data class DivvyFriend(
        val profile: ProfileRow,
        val sharedGroups: List<Group>
    ) : ContactEntry {
        override val displayName: String
            get() = "${profile.firstName} ${profile.lastName}".trim()
        override val selectionKey: String
            get() = "divvy_${profile.id}"
    }

    data class DeviceContact(
        val name: String,
        val contactValue: String,
        val contactType: ContactType
    ) : ContactEntry {
        override val displayName: String get() = name
        override val selectionKey: String
            get() = "device_${contactType.name}_$contactValue"
    }
}

enum class ContactType { PHONE, EMAIL }

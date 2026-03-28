package com.example.divvy.ui.groupdetail

import android.net.Uri

fun buildGroupInviteLink(groupId: String, groupName: String): String {
    val encodedName = Uri.encode(groupName)
    return "divvy://join/$groupId?groupName=$encodedName"
}

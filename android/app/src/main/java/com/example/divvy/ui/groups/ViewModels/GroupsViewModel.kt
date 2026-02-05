package com.example.divvy.ui.groups.ViewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GroupsViewModel : ViewModel() {
    private val _status = MutableStateFlow("Ready")
    val status: StateFlow<String> = _status
}

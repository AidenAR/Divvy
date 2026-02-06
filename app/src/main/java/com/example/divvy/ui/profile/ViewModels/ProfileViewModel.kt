package com.example.divvy.ui.profile.ViewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {
    private val _status = MutableStateFlow("Ready")
    val status: StateFlow<String> = _status
}

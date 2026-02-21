package com.example.divvy.ui.groupdetail.ViewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.divvy.backend.GroupDetailRepository
import com.example.divvy.models.ActivityItem
import com.example.divvy.models.Group
import com.example.divvy.models.MemberBalance
import com.example.divvy.ui.navigation.AppDestination
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val group: Group = Group(id = "", name = ""),
    val memberBalances: List<MemberBalance> = emptyList(),
    val activity: List<ActivityItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel(assistedFactory = GroupDetailViewModel.Factory::class)
class GroupDetailViewModel @AssistedInject constructor(
    @Assisted private val groupId: String,
    private val repo: GroupDetailRepository
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(groupId: String): GroupDetailViewModel
    }

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val group    = repo.getGroup(groupId)
            val balances = repo.getMemberBalances(groupId)
            val activity = repo.getActivity(groupId)
            _uiState.update { it.copy(group = group, memberBalances = balances,
                activity = activity, isLoading = false) }
        }
    }
}

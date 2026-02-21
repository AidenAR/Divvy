package com.example.divvy.ui.groupdetail.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.GroupRepository
import com.example.divvy.models.ActivityItem
import com.example.divvy.models.Group
import com.example.divvy.models.MemberBalance
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val group: Group = Group(id = "", name = ""),
    val memberBalances: List<MemberBalance> = emptyList(),
    val activity: List<ActivityItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel(assistedFactory = GroupDetailViewModel.Factory::class)
class GroupDetailViewModel @AssistedInject constructor(
    @Assisted private val groupId: String,
    private val repo: GroupRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(groupId: String): GroupDetailViewModel
    }

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.getGroup(groupId),
                repo.getMemberBalances(groupId),
                repo.getActivity(groupId)
            ) { group, balances, activity ->
                GroupDetailUiState(
                    group = group,
                    memberBalances = balances,
                    activity = activity,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }
}

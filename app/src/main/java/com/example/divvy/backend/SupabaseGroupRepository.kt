package com.example.divvy.backend

import com.example.divvy.BuildConfig
import com.example.divvy.components.GroupIcon
import com.example.divvy.models.ActivityItem
import com.example.divvy.models.Group
import com.example.divvy.models.GroupExpense
import com.example.divvy.models.GroupMember
import com.example.divvy.models.MemberBalance
import com.example.divvy.ui.auth.DummyAccount
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp

// ---------------------------------------------------------------------------
// DB row models
// ---------------------------------------------------------------------------

@Serializable
private data class GroupRow(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
private data class GroupMemberRow(
    @SerialName("group_id")   val groupId: String = "",
    @SerialName("user_id")    val userId: String = "",
    @SerialName("joined_at")  val joinedAt: String = ""
)

@Serializable
private data class ProfileNameRow(
    val id: String = "",
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name")  val lastName: String = ""
)

@Serializable
private data class NetBalanceRow(
    @SerialName("other_user_id")   val otherUserId: String = "",
    @SerialName("other_user_name") val otherUserName: String = "",
    @SerialName("net_cents")       val netCents: Long = 0L
)

// ---------------------------------------------------------------------------
// Repository
// ---------------------------------------------------------------------------

@Singleton
class SupabaseGroupRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val expensesRepository: ExpensesRepository
) : GroupRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val currentUserId: String
        get() = if (BuildConfig.AUTH_BYPASS) {
            DummyAccount.USER_ID
        } else {
            supabaseClient.auth.currentUserOrNull()?.id
                ?: error("No authenticated user")
        }

    private val _groups   = MutableStateFlow<List<Group>>(emptyList())
    private val _members  = MutableStateFlow<Map<String, List<GroupMember>>>(emptyMap())
    private val _expenses = MutableStateFlow<Map<String, List<GroupExpense>>>(emptyMap())

    init {
        scope.launch { refreshGroups() }
    }

    // ---------------------------------------------------------------------------
    // Groups
    // ---------------------------------------------------------------------------

    override fun listGroups(): Flow<List<Group>> = _groups

    override fun getGroup(groupId: String): Flow<Group> =
        _groups.map { list ->
            list.find { it.id == groupId } ?: Group(id = groupId, name = "Group")
        }

    override suspend fun createGroup(name: String, icon: GroupIcon): Group {
        val row = supabaseClient.from("groups")
            .insert(mapOf(
                "name"       to name,
                "icon"       to icon.name,
                "created_by" to currentUserId
            )) { select() }
            .decodeSingle<GroupRow>()

        val group = Group(
            id           = row.id,
            name         = row.name,
            icon         = iconFromName(row.icon),
            memberCount  = 1,
            balanceCents = 0L
        )
        _groups.update   { it + group }
        _members.update  { it + (row.id to emptyList()) }
        _expenses.update { it + (row.id to emptyList()) }
        return group
    }

    // ---------------------------------------------------------------------------
    // Members
    // ---------------------------------------------------------------------------

    override fun getMembers(groupId: String): Flow<List<GroupMember>> =
        _members.map { it[groupId] ?: emptyList() }

    override suspend fun addMember(groupId: String, member: GroupMember) {
        supabaseClient.from("group_members").insert(mapOf(
            "group_id" to groupId,
            "user_id"  to member.userId
        ))
        _members.update { map ->
            map + (groupId to ((map[groupId] ?: emptyList()) + member))
        }
    }

    // ---------------------------------------------------------------------------
    // Expenses
    // ---------------------------------------------------------------------------

    override suspend fun addExpense(expense: GroupExpense) {
        expensesRepository.createExpenseWithSplits(
            groupId     = expense.groupId,
            description = expense.title,
            amountCents = expense.amountCents,
            currency    = "USD",
            splitMethod = "EQUAL",
            splits      = expense.splits
        )
        refreshExpenses(expense.groupId)
    }

    override suspend fun leaveGroup(groupId: String) {
        supabaseClient.from("group_members").delete {
            filter {
                eq("group_id", groupId)
                eq("user_id", currentUserId)
            }
        }
        _groups.update   { it.filter { g -> g.id != groupId } }
        _members.update  { it - groupId }
        _expenses.update { it - groupId }
    }

    override fun getAllExpenses(): Flow<List<GroupExpense>> =
        _expenses.map { it.values.flatten() }

    // ---------------------------------------------------------------------------
    // Derived
    // ---------------------------------------------------------------------------

    override fun getMemberBalances(groupId: String): Flow<List<MemberBalance>> =
        _members.map { membersMap ->
            val members = membersMap[groupId] ?: return@map emptyList()
            val params = buildJsonObject { put("p_group_id", groupId) }
            val balanceRows = try {
                supabaseClient.postgrest
                    .rpc("net_balances", params)
                    .decodeList<NetBalanceRow>()
            } catch (e: Exception) {
                emptyList()
            }
            val balanceMap = balanceRows.associateBy { it.otherUserId }
            members.map { member ->
                MemberBalance(
                    userId       = member.userId,
                    name         = member.name,
                    balanceCents = balanceMap[member.userId]?.netCents ?: 0L
                )
            }
        }

    override fun getActivity(groupId: String): Flow<List<ActivityItem>> =
        combine(_members, _expenses) { members, expenses ->
            val expenseList = expenses[groupId] ?: return@combine emptyList()
            val memberMap   = (members[groupId] ?: emptyList()).associateBy { it.userId }
            val userId      = currentUserId
            expenseList
                .sortedByDescending { it.createdAt }
                .map { expense ->
                    val paidByCurrentUser = expense.paidByUserId == userId
                    ActivityItem(
                        id                = expense.id,
                        title             = expense.title,
                        amountCents       = expense.amountCents,
                        dateLabel         = dateLabel(expense.createdAt),
                        paidByLabel       = if (paidByCurrentUser) "You"
                        else memberMap[expense.paidByUserId]?.name ?: "Unknown",
                        paidByCurrentUser = paidByCurrentUser,
                        timestamp = expense.createdAt
                    )
                }
        }

    // ---------------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------------

    private suspend fun refreshGroups() {
        try {
            val rows = supabaseClient.from("groups")
                .select()
                .decodeList<GroupRow>()

            val groups = rows.map { row ->
                val members  = fetchMembers(row.id)
                val expenses = fetchExpenses(row.id)
                val balance  = computeBalance(currentUserId, expenses)

                _members.update  { it + (row.id to members) }
                _expenses.update { it + (row.id to expenses) }

                Group(
                    id           = row.id,
                    name         = row.name,
                    icon         = iconFromName(row.icon),
                    memberCount  = members.size + 1,
                    balanceCents = balance
                )
            }
            _groups.value = groups
        } catch (_: Exception) { }
    }

    private suspend fun refreshExpenses(groupId: String) {
        try {
            val expenses = fetchExpenses(groupId)
            _expenses.update { it + (groupId to expenses) }
            _groups.update { list ->
                list.map { g ->
                    if (g.id == groupId) g.copy(balanceCents = computeBalance(currentUserId, expenses))
                    else g
                }
            }
        } catch (_: Exception) { }
    }

    private suspend fun fetchMembers(groupId: String): List<GroupMember> {
        val memberRows = supabaseClient.from("group_members")
            .select { filter { eq("group_id", groupId) } }
            .decodeList<GroupMemberRow>()
            .filter { it.userId != currentUserId }

        if (memberRows.isEmpty()) return emptyList()

        // Fetch display names from profiles in a single query using in filter
        val userIds = memberRows.map { it.userId }
        val profiles = supabaseClient.from("profiles")
            .select { filter { isIn("id", userIds) } }
            .decodeList<ProfileNameRow>()
            .associateBy { it.id }

        return memberRows.map { row ->
            val profile = profiles[row.userId]
            val name = if (profile != null) {
                "${profile.firstName} ${profile.lastName}".trim()
            } else {
                row.userId
            }
            GroupMember(userId = row.userId, name = name)
        }
    }

    private suspend fun fetchExpenses(groupId: String): List<GroupExpense> =
        expensesRepository.listGroupExpenses(groupId)

    private fun computeBalance(userId: String, expenses: List<GroupExpense>): Long {
        var balance = 0L
        for (expense in expenses) {
            val myShare = expense.splits.find { it.userId == userId }?.amountCents ?: 0L
            if (expense.paidByUserId == userId) {
                balance += expense.amountCents - myShare
            } else {
                balance -= myShare
            }
        }
        return balance
    }

    private fun iconFromName(name: String): GroupIcon =
        GroupIcon.entries.find { it.name == name } ?: GroupIcon.Group

    private fun dateLabel(isoDate: String): String {
        return try {
            val date  = LocalDate.parse(isoDate.take(10))
            val today = LocalDate.now()
            when (date) {
                today              -> "Today"
                today.minusDays(1) -> "Yesterday"
                else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            }
        } catch (_: Exception) { isoDate }
    }
}
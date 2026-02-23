package com.example.divvy.backend

import com.example.divvy.FeatureFlags
import com.example.divvy.models.Expense
import com.example.divvy.models.ExpenseSplit
import com.example.divvy.models.GroupExpense
import com.example.divvy.ui.auth.DummyAccount
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import javax.inject.Inject

class SupabaseExpensesRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ExpensesRepository {

    private val currentUserId: String
        get() = if (FeatureFlags.AUTH_BYPASS) {
            DummyAccount.USER_ID
        } else {
            supabaseClient.auth.currentUserOrNull()?.id
                ?: error("No authenticated user — cannot access expenses")
        }

    override suspend fun listExpenses(): List<Expense> =
        supabaseClient.from("expenses").select().decodeList()

    override suspend fun listExpensesByGroup(groupId: String): List<Expense> =
        supabaseClient.from("expenses")
            .select { filter { eq("group_id", groupId) } }
            .decodeList()

    override suspend fun getExpenseById(expenseId: String): Expense? =
        supabaseClient.from("expenses")
            .select { filter { eq("id", expenseId) } }
            .decodeSingleOrNull()

    override suspend fun listGroupExpenses(groupId: String): List<GroupExpense> =
        supabaseClient.from("group_expenses_with_splits")
            .select { filter { eq("group_id", groupId) } }
            .decodeList()

    override suspend fun getGroupExpenseById(expenseId: String): GroupExpense? {
        val expense = getExpenseById(expenseId) ?: return null
        val splits = supabaseClient.from("expense_splits")
            .select { filter { eq("expense_id", expenseId) } }
            .decodeList<ExpenseSplit>()
        return GroupExpense(
            id = expense.id,
            groupId = expense.groupId,
            title = expense.merchant,
            amountCents = expense.amountCents,
            paidByUserId = expense.paidByUserId,
            splits = splits,
            createdAt = expense.createdAt
        )
    }

    override suspend fun createExpense(
        groupId: String,
        description: String,
        amountCents: Long,
        splitMethod: String
    ): Expense {
        val expense = Expense(
            groupId = groupId,
            merchant = description,
            amountCents = amountCents,
            splitMethod = splitMethod,
            currency = "USD",
            paidByUserId = currentUserId
        )
        return supabaseClient.from("expenses")
            .insert(expense) { select() }
            .decodeSingle()
    }

    override suspend fun createExpenseWithSplits(
        groupId: String,
        description: String,
        amountCents: Long,
        currency: String,
        splitMethod: String,
        splits: List<ExpenseSplit>
    ): GroupExpense {
        val params = buildJsonObject {
            put("p_group_id", groupId)
            put("p_merchant", description)
            put("p_amount_cents", amountCents)
            put("p_currency", currency)
            put("p_split_method", splitMethod)
            put("p_paid_by", currentUserId)
            putJsonArray("p_splits") {
                splits.forEach { split ->
                    add(buildJsonObject {
                        put("user_id", split.userId)
                        put("amount_cents", split.amountCents)
                        split.isCoveredBy?.let { put("is_covered_by", it) }
                    })
                }
            }
        }

        val expenseId = supabaseClient.postgrest
            .rpc("create_expense_with_splits", params)
            .decodeAs<String>()

        return getGroupExpenseById(expenseId)
            ?: error("Failed to fetch expense after creation: $expenseId")
    }

    override suspend fun updateExpense(
        expenseId: String,
        groupId: String,
        description: String,
        amountCents: Long,
        splitMethod: String,
        currency: String
    ): Expense =
        supabaseClient.from("expenses")
            .update({
                set("group_id", groupId)
                set("merchant", description)
                set("amount_cents", amountCents)
                set("split_method", splitMethod)
                set("currency", currency)
            }) {
                select()
                filter { eq("id", expenseId) }
            }
            .decodeSingle()

    override suspend fun updateExpenseSplits(expenseId: String, splits: List<ExpenseSplit>) {
        val params = buildJsonObject {
            put("p_expense_id", expenseId)
            putJsonArray("p_splits") {
                splits.forEach { split ->
                    add(buildJsonObject {
                        put("user_id", split.userId)
                        put("amount_cents", split.amountCents)
                        split.isCoveredBy?.let { put("is_covered_by", it) }
                    })
                }
            }
        }

        supabaseClient.postgrest.rpc("update_expense_splits", params)
    }

    override suspend fun deleteExpense(expenseId: String) {
        supabaseClient.from("expenses")
            .delete { filter { eq("id", expenseId) } }
    }
}
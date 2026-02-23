package com.example.divvy.backend

import com.example.divvy.models.Expense
import com.example.divvy.models.ExpenseSplit
import com.example.divvy.models.GroupExpense
import java.util.NoSuchElementException
import java.util.UUID
import javax.inject.Inject

interface ExpensesRepository {
    suspend fun listExpenses(): List<Expense>
    suspend fun listExpensesByGroup(groupId: String): List<Expense>
    suspend fun listGroupExpenses(groupId: String): List<GroupExpense>
    suspend fun createExpense(
        groupId: String,
        description: String,
        amountCents: Long,
        splitMethod: String
    ): Expense
    suspend fun createExpenseWithSplits(
        groupId: String,
        description: String,
        amountCents: Long,
        currency: String,
        splitMethod: String,
        splits: List<ExpenseSplit>
    ): GroupExpense
    suspend fun getExpenseById(expenseId: String): Expense?
    suspend fun getGroupExpenseById(expenseId: String): GroupExpense?
    suspend fun updateExpense(
        expenseId: String,
        groupId: String,
        description: String,
        amountCents: Long,
        splitMethod: String,
        currency: String
    ): Expense
    suspend fun updateExpenseSplits(expenseId: String, splits: List<ExpenseSplit>)
    suspend fun deleteExpense(expenseId: String)
}

class StubExpensesRepository @Inject constructor() : ExpensesRepository {

    private val expenses = mutableListOf<Expense>()
    private val splits   = mutableMapOf<String, List<ExpenseSplit>>()

    override suspend fun listExpenses(): List<Expense> =
        expenses.toList()

    override suspend fun listExpensesByGroup(groupId: String): List<Expense> =
        expenses.filter { it.groupId == groupId }

    override suspend fun listGroupExpenses(groupId: String): List<GroupExpense> =
        expenses.filter { it.groupId == groupId }.map { it.toGroupExpense() }

    override suspend fun createExpense(
        groupId: String,
        description: String,
        amountCents: Long,
        splitMethod: String
    ): Expense {
        val expense = Expense(
            id           = UUID.randomUUID().toString(),
            groupId      = groupId,
            merchant     = description,
            amountCents  = amountCents,
            splitMethod  = splitMethod,
            currency     = "USD",
            paidByUserId = "stub_user",
            createdAt    = System.currentTimeMillis().toString()
        )
        expenses.add(expense)
        return expense
    }

    override suspend fun createExpenseWithSplits(
        groupId: String,
        description: String,
        amountCents: Long,
        currency: String,
        splitMethod: String,
        splits: List<ExpenseSplit>
    ): GroupExpense {
        val expense = createExpense(groupId, description, amountCents, splitMethod)
        this.splits[expense.id] = splits
        return expense.toGroupExpense()
    }

    override suspend fun getExpenseById(expenseId: String): Expense? =
        expenses.find { it.id == expenseId }

    override suspend fun getGroupExpenseById(expenseId: String): GroupExpense? =
        expenses.find { it.id == expenseId }?.toGroupExpense()

    override suspend fun updateExpense(
        expenseId: String,
        groupId: String,
        description: String,
        amountCents: Long,
        splitMethod: String,
        currency: String
    ): Expense {
        val index = expenses.indexOfFirst { it.id == expenseId }
        if (index == -1) throw NoSuchElementException("Expense with ID $expenseId not found")
        val updated = expenses[index].copy(
            groupId     = groupId,
            merchant    = description,
            amountCents = amountCents,
            splitMethod = splitMethod,
            currency    = currency
        )
        expenses[index] = updated
        return updated
    }

    override suspend fun updateExpenseSplits(expenseId: String, splits: List<ExpenseSplit>) {
        if (expenses.none { it.id == expenseId })
            throw NoSuchElementException("Expense with ID $expenseId not found")
        this.splits[expenseId] = splits
    }

    override suspend fun deleteExpense(expenseId: String) {
        expenses.removeIf { it.id == expenseId }
        splits.remove(expenseId)
    }

    private fun Expense.toGroupExpense() = GroupExpense(
        id           = id,
        groupId      = groupId,
        title        = merchant,
        amountCents  = amountCents,
        paidByUserId = paidByUserId,
        splits       = splits[id] ?: emptyList(),
        createdAt    = createdAt
    )
}
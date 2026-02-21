package com.example.divvy.backend

import com.example.divvy.models.Expense
import java.util.UUID
import javax.inject.Inject

interface ExpensesRepository {
    suspend fun listExpenses(): List<Expense>
    suspend fun createExpense(
        groupId: String,
        description: String,
        amountCents: Long,
        splitMethod: String
    ): Expense
}

class StubExpensesRepository @Inject constructor() : ExpensesRepository {

    private val expenses = mutableListOf<Expense>()

    override suspend fun listExpenses(): List<Expense> = expenses.toList()

    override suspend fun createExpense(
        groupId: String,
        description: String,
        amountCents: Long,
        splitMethod: String
    ): Expense {
        val expense = Expense(
            id = UUID.randomUUID().toString(),
            groupId = groupId,
            merchant = description,
            amountCents = amountCents,
            splitMethod = splitMethod,
            currency = "USD",
            createdAt = System.currentTimeMillis().toString()
        )
        expenses.add(expense)
        return expense
    }
}

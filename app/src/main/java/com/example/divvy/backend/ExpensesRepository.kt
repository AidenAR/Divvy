package com.example.divvy.backend

import com.example.divvy.models.Expense

interface ExpensesRepository {
    suspend fun listExpenses(): List<Expense>
}

class StubExpensesRepository : ExpensesRepository {
    override suspend fun listExpenses(): List<Expense> = emptyList()
}

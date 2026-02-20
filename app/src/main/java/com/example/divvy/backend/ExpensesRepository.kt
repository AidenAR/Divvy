package com.example.divvy.backend

import com.example.divvy.models.Expense
import javax.inject.Inject

interface ExpensesRepository {
    suspend fun listExpenses(): List<Expense>
}

class StubExpensesRepository @Inject constructor() : ExpensesRepository {
    override suspend fun listExpenses(): List<Expense> = emptyList()
}

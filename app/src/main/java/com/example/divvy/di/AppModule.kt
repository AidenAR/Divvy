package com.example.divvy.di

import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.GroupsRepository
import com.example.divvy.backend.LedgerRepository
import com.example.divvy.backend.ProfilesRepository
import com.example.divvy.backend.StubExpensesRepository
import com.example.divvy.backend.StubGroupsRepository
import com.example.divvy.backend.StubLedgerRepository
import com.example.divvy.backend.SupabaseProfilesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds @Singleton abstract fun bindGroupsRepository(impl: StubGroupsRepository): GroupsRepository
    @Binds @Singleton abstract fun bindProfilesRepository(impl: SupabaseProfilesRepository): ProfilesRepository
    @Binds @Singleton abstract fun bindExpensesRepository(impl: StubExpensesRepository): ExpensesRepository
    @Binds @Singleton abstract fun bindLedgerRepository(impl: StubLedgerRepository): LedgerRepository
}

package com.example.divvy.di

import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.backend.ProfilesRepository
import com.example.divvy.backend.StubGroupRepository
import com.example.divvy.backend.SupabaseExpensesRepository
import com.example.divvy.backend.SupabaseProfilesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds @Singleton abstract fun bindGroupRepository(impl: StubGroupRepository): GroupRepository
    @Binds @Singleton abstract fun bindProfilesRepository(impl: SupabaseProfilesRepository): ProfilesRepository
    @Binds @Singleton abstract fun bindExpensesRepository(impl: SupabaseExpensesRepository): ExpensesRepository
}

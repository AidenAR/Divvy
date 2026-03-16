package com.example.divvy.di

import com.example.divvy.backend.ActivityRepository
import com.example.divvy.backend.AndroidContactsRepository
import com.example.divvy.backend.AuthRepository
import com.example.divvy.backend.BalanceRepository
import com.example.divvy.backend.ContactsRepository
import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.FriendsRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.backend.MemberRepository
import com.example.divvy.backend.ProfilesRepository
import com.example.divvy.backend.SupabaseActivityRepository
import com.example.divvy.backend.SupabaseAuthRepository
import com.example.divvy.backend.SupabaseBalanceRepository
import com.example.divvy.backend.SupabaseExpensesRepository
import com.example.divvy.backend.SupabaseFriendsRepository
import com.example.divvy.backend.SupabaseGroupRepository
import com.example.divvy.backend.SupabaseMemberRepository
import com.example.divvy.backend.DefaultStatementRepository
import com.example.divvy.backend.StatementRepository
import com.example.divvy.backend.SupabaseProfilesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository
    @Binds @Singleton abstract fun bindGroupRepository(impl: SupabaseGroupRepository): GroupRepository
    @Binds @Singleton abstract fun bindMemberRepository(impl: SupabaseMemberRepository): MemberRepository
    @Binds @Singleton abstract fun bindBalanceRepository(impl: SupabaseBalanceRepository): BalanceRepository
    @Binds @Singleton abstract fun bindExpensesRepository(impl: SupabaseExpensesRepository): ExpensesRepository
    @Binds @Singleton abstract fun bindProfilesRepository(impl: SupabaseProfilesRepository): ProfilesRepository
    @Binds @Singleton abstract fun bindActivityRepository(impl: SupabaseActivityRepository): ActivityRepository
    @Binds @Singleton abstract fun bindStatementRepository(impl: DefaultStatementRepository): StatementRepository
    @Binds @Singleton abstract fun bindFriendsRepository(impl: SupabaseFriendsRepository): FriendsRepository
    @Binds @Singleton abstract fun bindContactsRepository(impl: AndroidContactsRepository): ContactsRepository
}

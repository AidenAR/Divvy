package com.example.divvy.di

import com.example.divvy.backend.ActivityRepository
import com.example.divvy.backend.AndroidContactsRepository
import com.example.divvy.backend.AuthRepository
import com.example.divvy.backend.BalanceRepository
import com.example.divvy.backend.ContactsRepository
import com.example.divvy.backend.ForexRepository
import com.example.divvy.backend.FrankfurterForexRepository
import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.SettlementsRepository
import com.example.divvy.backend.SupabaseSettlementsRepository
import com.example.divvy.backend.FriendsRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.backend.MemberRepository
import com.example.divvy.backend.ProfilesRepository
import com.example.divvy.backend.SupabaseAuthRepository
import com.example.divvy.backend.SupabaseFriendsRepository
import com.example.divvy.backend.DefaultStatementRepository
import com.example.divvy.backend.StatementRepository
import com.example.divvy.backend.SupabaseProfilesRepository
import com.example.divvy.offline.repository.OfflineActivityRepository
import com.example.divvy.offline.repository.OfflineBalanceRepository
import com.example.divvy.offline.repository.OfflineExpensesRepository
import com.example.divvy.offline.repository.OfflineGroupRepository
import com.example.divvy.offline.repository.OfflineMemberRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository
    @Binds @Singleton abstract fun bindGroupRepository(impl: OfflineGroupRepository): GroupRepository
    @Binds @Singleton abstract fun bindMemberRepository(impl: OfflineMemberRepository): MemberRepository
    @Binds @Singleton abstract fun bindBalanceRepository(impl: OfflineBalanceRepository): BalanceRepository
    @Binds @Singleton abstract fun bindExpensesRepository(impl: OfflineExpensesRepository): ExpensesRepository
    @Binds @Singleton abstract fun bindProfilesRepository(impl: SupabaseProfilesRepository): ProfilesRepository
    @Binds @Singleton abstract fun bindActivityRepository(impl: OfflineActivityRepository): ActivityRepository
    @Binds @Singleton abstract fun bindStatementRepository(impl: DefaultStatementRepository): StatementRepository
    @Binds @Singleton abstract fun bindFriendsRepository(impl: SupabaseFriendsRepository): FriendsRepository
    @Binds @Singleton abstract fun bindContactsRepository(impl: AndroidContactsRepository): ContactsRepository
    @Binds @Singleton abstract fun bindForexRepository(impl: FrankfurterForexRepository): ForexRepository
    @Binds @Singleton abstract fun bindSettlementsRepository(impl: SupabaseSettlementsRepository): SettlementsRepository
}

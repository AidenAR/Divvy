package com.example.divvy.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.divvy.offline.db.DivvyDatabase
import com.example.divvy.offline.db.dao.CachedActivityDao
import com.example.divvy.offline.db.dao.CachedBalanceDao
import com.example.divvy.offline.db.dao.CachedExpenseDao
import com.example.divvy.offline.db.dao.CachedExpenseSplitDao
import com.example.divvy.offline.db.dao.CachedGroupDao
import com.example.divvy.offline.db.dao.CachedMemberDao
import com.example.divvy.offline.db.dao.PendingOperationDao
import com.example.divvy.security.SQLCipherPassphraseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OfflineModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DivvyDatabase {
        System.loadLibrary("sqlcipher")
        val passphrase = SQLCipherPassphraseProvider.getOrCreatePassphrase(context)
        val factory = SupportOpenHelperFactory(passphrase)
        @Suppress("DEPRECATION")
        return Room.databaseBuilder(
            context,
            DivvyDatabase::class.java,
            "divvy_cache.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideCachedGroupDao(db: DivvyDatabase): CachedGroupDao = db.cachedGroupDao()
    @Provides fun provideCachedExpenseDao(db: DivvyDatabase): CachedExpenseDao = db.cachedExpenseDao()
    @Provides fun provideCachedExpenseSplitDao(db: DivvyDatabase): CachedExpenseSplitDao = db.cachedExpenseSplitDao()
    @Provides fun provideCachedMemberDao(db: DivvyDatabase): CachedMemberDao = db.cachedMemberDao()
    @Provides fun provideCachedBalanceDao(db: DivvyDatabase): CachedBalanceDao = db.cachedBalanceDao()
    @Provides fun provideCachedActivityDao(db: DivvyDatabase): CachedActivityDao = db.cachedActivityDao()
    @Provides fun providePendingOperationDao(db: DivvyDatabase): PendingOperationDao = db.pendingOperationDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}

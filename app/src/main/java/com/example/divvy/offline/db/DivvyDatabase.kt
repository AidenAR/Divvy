package com.example.divvy.offline.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.divvy.offline.db.dao.CachedActivityDao
import com.example.divvy.offline.db.dao.CachedBalanceDao
import com.example.divvy.offline.db.dao.CachedExpenseDao
import com.example.divvy.offline.db.dao.CachedExpenseSplitDao
import com.example.divvy.offline.db.dao.CachedGroupDao
import com.example.divvy.offline.db.dao.CachedMemberDao
import com.example.divvy.offline.db.dao.PendingOperationDao
import com.example.divvy.offline.db.entity.CachedActivityEntity
import com.example.divvy.offline.db.entity.CachedBalanceEntity
import com.example.divvy.offline.db.entity.CachedExpenseEntity
import com.example.divvy.offline.db.entity.CachedExpenseSplitEntity
import com.example.divvy.offline.db.entity.CachedGroupEntity
import com.example.divvy.offline.db.entity.CachedMemberEntity
import com.example.divvy.offline.db.entity.PendingOperationEntity

@Database(
    entities = [
        CachedGroupEntity::class,
        CachedExpenseEntity::class,
        CachedExpenseSplitEntity::class,
        CachedMemberEntity::class,
        CachedBalanceEntity::class,
        CachedActivityEntity::class,
        PendingOperationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DivvyDatabase : RoomDatabase() {
    abstract fun cachedGroupDao(): CachedGroupDao
    abstract fun cachedExpenseDao(): CachedExpenseDao
    abstract fun cachedExpenseSplitDao(): CachedExpenseSplitDao
    abstract fun cachedMemberDao(): CachedMemberDao
    abstract fun cachedBalanceDao(): CachedBalanceDao
    abstract fun cachedActivityDao(): CachedActivityDao
    abstract fun pendingOperationDao(): PendingOperationDao
}

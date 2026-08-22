package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserAccountEntity::class,
        SecurityAuditEntity::class,
        SmsCommandHistoryEntity::class,
        DeviceSettingEntity::class,
        VaultItemEntity::class,
        ActivationCodeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun securityAuditDao(): SecurityAuditDao
    abstract fun smsCommandHistoryDao(): SmsCommandHistoryDao
    abstract fun deviceSettingDao(): DeviceSettingDao
    abstract fun vaultDao(): VaultDao
    abstract fun activationCodeDao(): ActivationCodeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anti_theft_mz_pro.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

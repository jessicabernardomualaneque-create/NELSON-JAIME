package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserAccountEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserAccountEntity?

    @Query("SELECT COUNT(*) FROM users")
    fun getUsersCount(): Flow<Int>

    @Query("SELECT * FROM users WHERE isPaymentConfirmed = 0 AND accountStatus = 'PENDENTE_CONFIRMACAO' ORDER BY createdAt DESC")
    fun getPendingPaymentUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE isPaymentConfirmed = 0 AND accountStatus = 'PENDENTE_CONFIRMACAO'")
    fun getPendingPaymentCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity): Long

    @Update
    suspend fun updateUser(user: UserAccountEntity)

    @Query("UPDATE users SET accountStatus = :status, planType = :planType, planExpiry = :expiry, isPaymentConfirmed = :confirmed, paymentConfirmedAt = :confirmedAt, paymentConfirmedBy = :confirmedBy, notes = :notes WHERE phoneNumber = :phone")
    suspend fun updateUserSubscription(
        phone: String,
        status: String,
        planType: String,
        expiry: Long,
        confirmed: Boolean,
        confirmedAt: Long,
        confirmedBy: String,
        notes: String
    )

    @Delete
    suspend fun deleteUser(user: UserAccountEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Long)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

@Dao
interface SecurityAuditDao {
    @Query("SELECT * FROM security_audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SecurityAuditEntity>>

    @Query("SELECT * FROM security_audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<SecurityAuditEntity>>

    @Query("SELECT COUNT(*) FROM security_audit_logs")
    fun getLogsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SecurityAuditEntity): Long

    @Query("DELETE FROM security_audit_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM security_audit_logs")
    suspend fun clearAllLogs()
}

@Dao
interface SmsCommandHistoryDao {
    @Query("SELECT * FROM sms_commands_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<SmsCommandHistoryEntity>>

    @Query("SELECT COUNT(*) FROM sms_commands_history")
    fun getCommandsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(cmd: SmsCommandHistoryEntity): Long

    @Query("DELETE FROM sms_commands_history")
    suspend fun clearHistory()
}

@Dao
interface DeviceSettingDao {
    @Query("SELECT * FROM device_settings")
    fun getAllSettings(): Flow<List<DeviceSettingEntity>>

    @Query("SELECT value FROM device_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: DeviceSettingEntity)

    @Query("DELETE FROM device_settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)
}

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY hiddenAt DESC")
    fun getAllVaultItems(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE itemType = :type ORDER BY hiddenAt DESC")
    fun getVaultItemsByType(type: String): Flow<List<VaultItemEntity>>

    @Query("SELECT COUNT(*) FROM vault_items WHERE itemType = :type")
    fun getCountByType(type: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM vault_items")
    fun getTotalVaultCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItemEntity): Long

    @Update
    suspend fun updateVaultItem(item: VaultItemEntity)

    @Delete
    suspend fun deleteVaultItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteVaultItemById(id: Long)

    @Query("UPDATE vault_items SET isLocked = :isLocked WHERE id = :id")
    suspend fun toggleLockStatus(id: Long, isLocked: Boolean)
}

@Dao
interface ActivationCodeDao {
    @Query("SELECT * FROM activation_codes ORDER BY createdAt DESC")
    fun getAllCodes(): Flow<List<ActivationCodeEntity>>

    @Query("SELECT * FROM activation_codes WHERE code = :code LIMIT 1")
    suspend fun getCodeByValue(code: String): ActivationCodeEntity?

    @Query("SELECT COUNT(*) FROM activation_codes")
    fun getCodesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCode(code: ActivationCodeEntity): Long

    @Update
    suspend fun updateCode(code: ActivationCodeEntity)

    @Delete
    suspend fun deleteCode(code: ActivationCodeEntity)

    @Query("DELETE FROM activation_codes WHERE id = :id")
    suspend fun deleteCodeById(id: Long)

    @Query("DELETE FROM activation_codes")
    suspend fun deleteAllCodes()
}

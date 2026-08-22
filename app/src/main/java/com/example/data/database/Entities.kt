package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phoneNumber: String,
    val operator: String,
    val pin: String,
    val isVerified: Boolean = true,
    val role: String = "USER", // "ADMIN" or "USER"
    val accountStatus: String = "ATIVO", // "ATIVO", "PENDENTE_CONFIRMACAO", "EXPIRADO", "TRIAL"
    val planType: String = "TRIAL", // "DIARIO", "SEMANAL", "MENSAL", "ILIMITADO"
    val planExpiry: Long = 0L,
    val isPaymentConfirmed: Boolean = false,
    val paymentConfirmedAt: Long? = null,
    val paymentConfirmedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis(),
    val batteryPct: Int = 85,
    val isArmed: Boolean = true,
    val notes: String = "Dispositivo Ativo em Moçambique"
)

@Entity(tableName = "security_audit_logs")
data class SecurityAuditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val locationInfo: String? = null,
    val senderNumber: String? = null,
    val severity: String = "INFO" // "INFO", "WARNING", "CRITICAL"
)

@Entity(tableName = "sms_commands_history")
data class SmsCommandHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val command: String,
    val senderNumber: String,
    val responseMessage: String,
    val batteryPct: Int,
    val isCharging: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean = true
)

@Entity(tableName = "device_settings")
data class DeviceSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val description: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemType: String, // "PHOTO", "MESSAGE", "MUSIC", "APP_LOCK"
    val title: String,
    val subtitle: String = "",
    val secretContent: String, // encrypted content, base64 data, notes or package name
    val originalFileName: String? = null,
    val fileSizeBytes: Long = 0L,
    val isEncrypted: Boolean = true,
    val hiddenAt: Long = System.currentTimeMillis(),
    val isLocked: Boolean = true,
    val extraTag: String = ""
)

@Entity(tableName = "activation_codes")
data class ActivationCodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String, // e.g. "MZ-84A7-9K2P"
    val planType: String, // "DIARIO", "SEMANAL", "MENSAL", "UNLIMITED"
    val durationDays: Int, // 1, 7, 30, 9999
    val priceMt: Int, // 7, 25, 80, 0
    val status: String = "PENDENTE", // "PENDENTE", "USADO"
    val createdAt: Long = System.currentTimeMillis(),
    val usedAt: Long? = null,
    val usedByPhone: String? = null
)

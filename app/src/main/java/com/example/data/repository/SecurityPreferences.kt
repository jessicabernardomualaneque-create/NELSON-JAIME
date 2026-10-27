package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.EmergencyContact
import com.example.data.model.EventType
import com.example.data.model.MozambiqueOperator
import com.example.data.model.MozambiquePhoneHelper
import com.example.data.model.SecurityEvent
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SecurityPreferences(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("anti_theft_mz_pro_prefs", Context.MODE_PRIVATE)

    private val _isArmedFlow = MutableStateFlow(isArmed())
    val isArmedFlow: StateFlow<Boolean> = _isArmedFlow.asStateFlow()

    private val _eventsFlow = MutableStateFlow(getEvents())
    val eventsFlow: StateFlow<List<SecurityEvent>> = _eventsFlow.asStateFlow()

    private val _contactsFlow = MutableStateFlow(getEmergencyContacts())
    val contactsFlow: StateFlow<List<EmergencyContact>> = _contactsFlow.asStateFlow()

    private val _userProfileFlow = MutableStateFlow(getUserProfile())
    val userProfileFlow: StateFlow<UserProfile?> = _userProfileFlow.asStateFlow()

    private val _antiPowerOffFlow = MutableStateFlow(isAntiPowerOffEnabled())
    val antiPowerOffFlow: StateFlow<Boolean> = _antiPowerOffFlow.asStateFlow()

    private val _antiAirplaneModeFlow = MutableStateFlow(isAntiAirplaneModeEnabled())
    val antiAirplaneModeFlow: StateFlow<Boolean> = _antiAirplaneModeFlow.asStateFlow()

    private val _antiDataOffFlow = MutableStateFlow(isAntiDataOffEnabled())
    val antiDataOffFlow: StateFlow<Boolean> = _antiDataOffFlow.asStateFlow()

    companion object {
        private const val KEY_PIN = "key_security_pin"
        private const val KEY_IS_ARMED = "key_is_armed"
        private const val KEY_TERMS_ACCEPTED = "key_terms_accepted"
        private const val KEY_ANTI_BRUTE_FORCE = "key_anti_brute_force"
        private const val KEY_SIM_WATCHDOG = "key_sim_watchdog"
        private const val KEY_STEALTH_MODE = "key_stealth_mode"
        private const val KEY_ANTI_POWER_OFF = "key_anti_power_off"
        private const val KEY_ANTI_AIRPLANE_MODE = "key_anti_airplane_mode"
        private const val KEY_ANTI_DATA_OFF = "key_anti_data_off"
        private const val KEY_LAST_KNOWN_SIM = "key_last_known_sim"
        private const val KEY_LAST_KNOWN_OPERATOR = "key_last_known_operator"
        private const val KEY_EMERGENCY_CONTACTS = "key_emergency_contacts"
        private const val KEY_SECURITY_EVENTS = "key_security_events"
        private const val KEY_OWNER_EMAIL = "key_owner_email"
        private const val KEY_FAILED_PIN_ATTEMPTS = "key_failed_pin_attempts"
        private const val KEY_USER_PROFILE = "key_user_profile"
        private const val KEY_REMEMBERED_PHONE = "key_remembered_phone"
        private const val KEY_REMEMBERED_PIN = "key_remembered_pin"
        private const val KEY_REMEMBERED_NAME = "key_remembered_name"
        private const val KEY_DEVICE_IMEI = "key_device_imei"
        private const val KEY_DEVICE_IMEI_2 = "key_device_imei_2"
        private const val KEY_DEVICE_BRAND = "key_device_brand"
        private const val KEY_DEVICE_MODEL = "key_device_model"
        private const val DEFAULT_PIN = "1234"
    }

    // User Profile Authentication (Mozambique Restricted)
    fun saveRememberedCredentials(phone: String, pin: String, name: String = "") {
        prefs.edit()
            .putString(KEY_REMEMBERED_PHONE, phone)
            .putString(KEY_REMEMBERED_PIN, pin)
            .putString(KEY_REMEMBERED_NAME, name)
            .apply()
    }

    fun getRememberedPhone(): String {
        return prefs.getString(KEY_REMEMBERED_PHONE, "") ?: ""
    }

    fun getRememberedPin(): String {
        return prefs.getString(KEY_REMEMBERED_PIN, "") ?: ""
    }

    fun getRememberedName(): String {
        return prefs.getString(KEY_REMEMBERED_NAME, "") ?: ""
    }

    fun clearRememberedCredentials() {
        prefs.edit()
            .remove(KEY_REMEMBERED_PHONE)
            .remove(KEY_REMEMBERED_PIN)
            .remove(KEY_REMEMBERED_NAME)
            .apply()
    }

    fun getUserProfile(): UserProfile? {
        val jsonStr = prefs.getString(KEY_USER_PROFILE, null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            val operatorName = json.optString("operator", "UNKNOWN")
            val op = try {
                MozambiqueOperator.valueOf(operatorName)
            } catch (e: Exception) {
                MozambiqueOperator.UNKNOWN
            }
            UserProfile(
                fullName = json.getString("fullName"),
                phoneNumber = json.getString("phoneNumber"),
                operator = op,
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                isVerified = json.optBoolean("isVerified", true)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        try {
            val json = JSONObject().apply {
                put("fullName", profile.fullName)
                put("phoneNumber", profile.phoneNumber)
                put("operator", profile.operator.name)
                put("createdAt", profile.createdAt)
                put("isVerified", profile.isVerified)
            }
            prefs.edit().putString(KEY_USER_PROFILE, json.toString()).apply()
            _userProfileFlow.value = profile
            setLastKnownOperator(profile.operator.displayName)
            logEvent(
                title = "Conta Moçambique Criada",
                description = "Conta associada ao número ${profile.phoneNumber} (${profile.operator.displayName}) criada com sucesso.",
                type = EventType.ACCOUNT_CREATED
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearUserProfile() {
        prefs.edit().remove(KEY_USER_PROFILE).apply()
        _userProfileFlow.value = null
    }

    fun isUserLoggedIn(): Boolean {
        return getUserProfile() != null
    }

    fun getSecurityPin(): String {
        return prefs.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setSecurityPin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun isArmed(): Boolean {
        return prefs.getBoolean(KEY_IS_ARMED, true)
    }

    fun setArmed(armed: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ARMED, armed).apply()
        _isArmedFlow.value = armed
        logEvent(
            title = if (armed) "Sistema Armado" else "Sistema Desarmado",
            description = if (armed) "Proteção ativa contra roubo e escuta de comandos SMS." else "Proteção temporariamente pausada pelo usuário.",
            type = if (armed) EventType.ARMED else EventType.DISARMED
        )
    }

    fun isTermsAccepted(): Boolean {
        return prefs.getBoolean(KEY_TERMS_ACCEPTED, false)
    }

    fun setTermsAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_TERMS_ACCEPTED, accepted).apply()
    }

    fun isAntiBruteForceEnabled(): Boolean {
        return prefs.getBoolean(KEY_ANTI_BRUTE_FORCE, true)
    }

    fun setAntiBruteForceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANTI_BRUTE_FORCE, enabled).apply()
    }

    fun isSimWatchdogEnabled(): Boolean {
        return prefs.getBoolean(KEY_SIM_WATCHDOG, true)
    }

    fun setSimWatchdogEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SIM_WATCHDOG, enabled).apply()
    }

    fun isStealthModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_STEALTH_MODE, false)
    }

    fun setStealthModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STEALTH_MODE, enabled).apply()
        if (enabled) {
            logEvent(
                title = "Modo Furtivo Ativado",
                description = "Dispositivo operando em silêncio com resposta contínua por SMS.",
                type = EventType.STEALTH_MODE_ACTIVE
            )
        }
    }

    fun isAntiPowerOffEnabled(): Boolean {
        return prefs.getBoolean(KEY_ANTI_POWER_OFF, true)
    }

    fun setAntiPowerOffEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANTI_POWER_OFF, enabled).apply()
        _antiPowerOffFlow.value = enabled
        logEvent(
            title = if (enabled) "Anti-Desligar Ativado" else "Anti-Desligar Desativado",
            description = if (enabled) "Bloqueio e alarme ao tentar desligar/apagar tela ativados." else "Proteção contra desligamento pausada.",
            type = if (enabled) EventType.ARMED else EventType.DISARMED
        )
    }

    fun isAntiAirplaneModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_ANTI_AIRPLANE_MODE, true)
    }

    fun setAntiAirplaneModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANTI_AIRPLANE_MODE, enabled).apply()
        _antiAirplaneModeFlow.value = enabled
        logEvent(
            title = if (enabled) "Anti-Modo Avião Ativado" else "Anti-Modo Avião Desativado",
            description = if (enabled) "Reversão automática e alerta por SMS ao ligar Modo Avião ativados." else "Proteção contra Modo Avião pausada.",
            type = if (enabled) EventType.ARMED else EventType.DISARMED
        )
    }

    fun isAntiDataOffEnabled(): Boolean {
        return prefs.getBoolean(KEY_ANTI_DATA_OFF, true)
    }

    fun setAntiDataOffEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANTI_DATA_OFF, enabled).apply()
        _antiDataOffFlow.value = enabled
        logEvent(
            title = if (enabled) "Anti-Desligar Dados Ativado" else "Anti-Desligar Dados Desativado",
            description = if (enabled) "Monitoramento de corte de internet móvel ativado." else "Proteção contra desligar dados pausada.",
            type = if (enabled) EventType.ARMED else EventType.DISARMED
        )
    }

    fun getOwnerEmail(): String {
        return prefs.getString(KEY_OWNER_EMAIL, "") ?: ""
    }

    fun setOwnerEmail(email: String) {
        prefs.edit().putString(KEY_OWNER_EMAIL, email).apply()
    }

    fun getLastKnownSim(): String {
        return prefs.getString(KEY_LAST_KNOWN_SIM, "") ?: ""
    }

    fun setLastKnownSim(simId: String) {
        prefs.edit().putString(KEY_LAST_KNOWN_SIM, simId).apply()
    }

    fun getLastKnownOperator(): String {
        return prefs.getString(KEY_LAST_KNOWN_OPERATOR, "Vodacom MZ") ?: "Vodacom MZ"
    }

    fun setLastKnownOperator(op: String) {
        prefs.edit().putString(KEY_LAST_KNOWN_OPERATOR, op).apply()
    }

    fun getDeviceImei(): String {
        return prefs.getString(KEY_DEVICE_IMEI, "") ?: ""
    }

    fun setDeviceImei(imei: String) {
        prefs.edit().putString(KEY_DEVICE_IMEI, imei).apply()
    }

    fun getDeviceImei2(): String {
        return prefs.getString(KEY_DEVICE_IMEI_2, "") ?: ""
    }

    fun setDeviceImei2(imei: String) {
        prefs.edit().putString(KEY_DEVICE_IMEI_2, imei).apply()
    }

    fun getDeviceBrand(): String {
        return prefs.getString(KEY_DEVICE_BRAND, "") ?: ""
    }

    fun setDeviceBrand(brand: String) {
        prefs.edit().putString(KEY_DEVICE_BRAND, brand).apply()
    }

    fun getDeviceModel(): String {
        return prefs.getString(KEY_DEVICE_MODEL, "") ?: ""
    }

    fun setDeviceModel(model: String) {
        prefs.edit().putString(KEY_DEVICE_MODEL, model).apply()
    }

    fun getFailedAttemptsCount(): Int {
        return prefs.getInt(KEY_FAILED_PIN_ATTEMPTS, 0)
    }

    fun incrementFailedAttempts(): Int {
        val current = getFailedAttemptsCount() + 1
        prefs.edit().putInt(KEY_FAILED_PIN_ATTEMPTS, current).apply()
        return current
    }

    fun resetFailedAttempts() {
        prefs.edit().putInt(KEY_FAILED_PIN_ATTEMPTS, 0).apply()
    }

    // Emergency Contacts Management
    fun getEmergencyContacts(): List<EmergencyContact> {
        val jsonString = prefs.getString(KEY_EMERGENCY_CONTACTS, null)
        if (jsonString.isNullOrEmpty()) {
            val defaultContacts = listOf(
                EmergencyContact(
                    id = "c1",
                    name = "Contacto de Confiança (MZ)",
                    phoneNumber = "+258 84 123 4567",
                    relationship = "Familiar"
                )
            )
            saveEmergencyContacts(defaultContacts)
            return defaultContacts
        }
        val list = mutableListOf<EmergencyContact>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    EmergencyContact(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        phoneNumber = obj.getString("phoneNumber"),
                        relationship = obj.optString("relationship", "Familiar")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveEmergencyContacts(contacts: List<EmergencyContact>) {
        try {
            val array = JSONArray()
            for (c in contacts) {
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("phoneNumber", c.phoneNumber)
                    put("relationship", c.relationship)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_EMERGENCY_CONTACTS, array.toString()).apply()
            _contactsFlow.value = contacts
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addEmergencyContact(name: String, phoneNumber: String, relationship: String) {
        val current = getEmergencyContacts().toMutableList()
        val newContact = EmergencyContact(
            id = UUID.randomUUID().toString(),
            name = name,
            phoneNumber = phoneNumber,
            relationship = relationship
        )
        current.add(newContact)
        saveEmergencyContacts(current)
    }

    fun removeEmergencyContact(contactId: String) {
        val current = getEmergencyContacts().toMutableList()
        current.removeAll { it.id == contactId }
        saveEmergencyContacts(current)
    }

    // Security Audit & Events Log
    fun getEvents(): List<SecurityEvent> {
        val jsonString = prefs.getString(KEY_SECURITY_EVENTS, null)
        if (jsonString.isNullOrEmpty()) {
            val defaultEvents = listOf(
                SecurityEvent(
                    id = "init",
                    title = "Sistema Inicializado",
                    description = "Módulo Anti-Roubo MZ Pro ativado com suporte a comandos offline.",
                    timestamp = System.currentTimeMillis() - 3600000,
                    type = EventType.ARMED
                )
            )
            saveEvents(defaultEvents)
            return defaultEvents
        }
        val list = mutableListOf<SecurityEvent>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SecurityEvent(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        timestamp = obj.getLong("timestamp"),
                        type = EventType.valueOf(obj.getString("type")),
                        locationInfo = if (obj.has("locationInfo")) obj.getString("locationInfo") else null,
                        senderNumber = if (obj.has("senderNumber")) obj.getString("senderNumber") else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.timestamp }
    }

    private fun saveEvents(events: List<SecurityEvent>) {
        try {
            val array = JSONArray()
            val trimmed = events.take(50)
            for (ev in trimmed) {
                val obj = JSONObject().apply {
                    put("id", ev.id)
                    put("title", ev.title)
                    put("description", ev.description)
                    put("timestamp", ev.timestamp)
                    put("type", ev.type.name)
                    ev.locationInfo?.let { put("locationInfo", it) }
                    ev.senderNumber?.let { put("senderNumber", it) }
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_SECURITY_EVENTS, array.toString()).apply()
            _eventsFlow.value = trimmed
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logEvent(
        title: String,
        description: String,
        type: EventType,
        locationInfo: String? = null,
        senderNumber: String? = null
    ) {
        val current = getEvents().toMutableList()
        val newEvent = SecurityEvent(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            timestamp = System.currentTimeMillis(),
            type = type,
            locationInfo = locationInfo,
            senderNumber = senderNumber
        )
        current.add(0, newEvent)
        saveEvents(current)
    }

    fun clearEvents() {
        saveEvents(emptyList())
    }
}

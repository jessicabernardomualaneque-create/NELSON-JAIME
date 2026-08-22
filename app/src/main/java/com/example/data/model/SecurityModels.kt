package com.example.data.model

data class EmergencyContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String = "Familiar"
)

enum class EventType {
    ARMED,
    DISARMED,
    SMS_COMMAND_RECEIVED,
    SMS_LOCATION_DISPATCHED,
    ALARM_TRIGGERED,
    FLASH_TRIGGERED,
    WRONG_PIN_ATTEMPT,
    SIM_CHANGED_ALERT,
    STEALTH_MODE_ACTIVE,
    DEVICE_LOCKED,
    ACCOUNT_CREATED,
    ACCOUNT_LOGGED_IN,
    ANTI_POWER_OFF_ALERT,
    ANTI_AIRPLANE_MODE_ALERT,
    ANTI_DATA_OFF_ALERT,
    CAMERA_CAPTURE_ALERT
}

data class SecurityEvent(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val type: EventType,
    val locationInfo: String? = null,
    val senderNumber: String? = null
)

data class SimCardInfo(
    val operatorName: String,
    val simSerialNumber: String,
    val subscriberId: String,
    val countryIso: String
)

data class SmsCommandItem(
    val command: String,
    val syntax: String,
    val description: String,
    val example: String,
    val badge: String
)

enum class MozambiqueOperator(val displayName: String, val prefixes: List<String>, val brandColor: Long) {
    VODACOM("Vodacom Moçambique", listOf("84", "85"), 0xFFE60000),
    MOVITEL("Movitel Moçambique", listOf("86", "87"), 0xFFFF6F00),
    TMCEL("Tmcel Moçambique", listOf("82", "83"), 0xFF007ACC),
    UNKNOWN("Desconhecido", emptyList(), 0xFF757575)
}

data class MozambiqueValidationResult(
    val isValid: Boolean,
    val formattedNumber: String,
    val operator: MozambiqueOperator,
    val errorMessage: String? = null
)

data class UserProfile(
    val fullName: String,
    val phoneNumber: String, // format: +258 84XXXXXXX
    val operator: MozambiqueOperator,
    val createdAt: Long = System.currentTimeMillis(),
    val isVerified: Boolean = true
)

object MozambiquePhoneHelper {
    const val COUNTRY_CODE = "+258"

    fun cleanDigits(input: String): String {
        return input.replace("[^0-9]".toRegex(), "")
    }

    fun getNationalDigits(input: String): String {
        var digits = cleanDigits(input)
        if (digits.startsWith("258")) {
            digits = digits.substring(3)
        }
        return digits
    }

    fun detectOperator(input: String): MozambiqueOperator {
        val nationalDigits = getNationalDigits(input)
        if (nationalDigits.length < 2) return MozambiqueOperator.UNKNOWN
        val prefix = nationalDigits.take(2)
        return when (prefix) {
            "84", "85" -> MozambiqueOperator.VODACOM
            "86", "87" -> MozambiqueOperator.MOVITEL
            "82", "83" -> MozambiqueOperator.TMCEL
            else -> MozambiqueOperator.UNKNOWN
        }
    }

    fun validate(input: String): MozambiqueValidationResult {
        val nationalDigits = getNationalDigits(input)
        if (nationalDigits.isEmpty()) {
            return MozambiqueValidationResult(
                isValid = false,
                formattedNumber = "",
                operator = MozambiqueOperator.UNKNOWN,
                errorMessage = "Por favor, digite seu número de telefone de Moçambique."
            )
        }

        if (nationalDigits.length < 2) {
            return MozambiqueValidationResult(
                isValid = false,
                formattedNumber = "$COUNTRY_CODE $nationalDigits",
                operator = MozambiqueOperator.UNKNOWN,
                errorMessage = "Prefixo inválido. Use 84/85 (Vodacom), 86/87 (Movitel) ou 82/83 (Tmcel)."
            )
        }

        val operator = detectOperator(input)
        if (operator == MozambiqueOperator.UNKNOWN) {
            val prefix = nationalDigits.take(2)
            return MozambiqueValidationResult(
                isValid = false,
                formattedNumber = "$COUNTRY_CODE $nationalDigits",
                operator = MozambiqueOperator.UNKNOWN,
                errorMessage = "Prefixo '$prefix' não é permitido. Apenas números de Moçambique (+258 84/85/86/87/82/83) são aceitos."
            )
        }

        if (nationalDigits.length != 9) {
            return MozambiqueValidationResult(
                isValid = false,
                formattedNumber = formatNational(nationalDigits),
                operator = operator,
                errorMessage = "O número de Moçambique deve conter exatamente 9 dígitos (atual: ${nationalDigits.length}/9)."
            )
        }

        return MozambiqueValidationResult(
            isValid = true,
            formattedNumber = formatNational(nationalDigits),
            operator = operator,
            errorMessage = null
        )
    }

    fun formatNational(nationalDigits: String): String {
        return if (nationalDigits.length <= 2) {
            "$COUNTRY_CODE $nationalDigits"
        } else if (nationalDigits.length <= 5) {
            "$COUNTRY_CODE ${nationalDigits.substring(0, 2)} ${nationalDigits.substring(2)}"
        } else {
            val endIdx = minOf(9, nationalDigits.length)
            val p1 = nationalDigits.substring(0, 2)
            val p2 = nationalDigits.substring(2, minOf(5, endIdx))
            val p3 = if (endIdx > 5) nationalDigits.substring(5, endIdx) else ""
            "$COUNTRY_CODE $p1 $p2 $p3".trim()
        }
    }
}

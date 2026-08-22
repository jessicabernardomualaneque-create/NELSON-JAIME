package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MozambiqueOperator
import com.example.data.model.MozambiquePhoneHelper
import com.example.data.repository.SecurityPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Anti-Roubo MZ Pro", appName)
  }

  @Test
  fun `verify security preferences initialized`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = SecurityPreferences(context)
    assertNotNull(prefs.getSecurityPin())
    assertEquals("1234", prefs.getSecurityPin())
  }

  @Test
  fun `verify mozambique phone numbers validation`() {
    // Vodacom (84, 85)
    val vodacomTest = MozambiquePhoneHelper.validate("+258841234567")
    assertTrue(vodacomTest.isValid)
    assertEquals(MozambiqueOperator.VODACOM, vodacomTest.operator)

    // Movitel (86, 87)
    val movitelTest = MozambiquePhoneHelper.validate("861234567")
    assertTrue(movitelTest.isValid)
    assertEquals(MozambiqueOperator.MOVITEL, movitelTest.operator)

    // Tmcel (82, 83)
    val tmcelTest = MozambiquePhoneHelper.validate("258821234567")
    assertTrue(tmcelTest.isValid)
    assertEquals(MozambiqueOperator.TMCEL, tmcelTest.operator)

    // Non-Mozambique / Invalid prefix
    val invalidPrefix = MozambiquePhoneHelper.validate("811234567")
    assertFalse(invalidPrefix.isValid)

    // Invalid length
    val invalidLength = MozambiquePhoneHelper.validate("84123")
    assertFalse(invalidLength.isValid)
  }

  @Test
  fun `verify account auto-registration persists and logs in directly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = SecurityPreferences(context)
    
    val profile = com.example.data.model.UserProfile(
      fullName = "Jéssica Bernardo",
      phoneNumber = "+258 84 123 4567",
      operator = MozambiqueOperator.VODACOM,
      isVerified = true
    )
    prefs.saveUserProfile(profile)
    prefs.setSecurityPin("5678")

    val retrieved = prefs.getUserProfile()
    assertNotNull(retrieved)
    assertEquals("Jéssica Bernardo", retrieved?.fullName)
    assertEquals("+258 84 123 4567", retrieved?.phoneNumber)
    assertEquals(MozambiqueOperator.VODACOM, retrieved?.operator)
    assertEquals("5678", prefs.getSecurityPin())
  }

  @Test
  fun `verify admin authentication strictly accepts designated credentials`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val adminRepo = com.example.data.database.AdminDatabaseRepository(context)

    // Valid Administrator credentials
    val validLogin = adminRepo.loginAdmin(
      email = "jessicabernardomualaneque@gmail.com",
      password = "123456789@#"
    )
    assertTrue(validLogin)
    assertTrue(adminRepo.isAdminLoggedIn.value)

    adminRepo.logoutAdmin()
    assertFalse(adminRepo.isAdminLoggedIn.value)

    // Case insensitivity on email
    val validCaseInsensitive = adminRepo.loginAdmin(
      email = "JessicaBernardoMualaneque@Gmail.com",
      password = "123456789@#"
    )
    assertTrue(validCaseInsensitive)

    // Invalid email
    val invalidEmail = adminRepo.loginAdmin(
      email = "outro.admin@gmail.com",
      password = "123456789@#"
    )
    assertFalse(invalidEmail)

    // Invalid password
    val invalidPassword = adminRepo.loginAdmin(
      email = "jessicabernardomualaneque@gmail.com",
      password = "wrongPassword123"
    )
    assertFalse(invalidPassword)
  }

  @Test
  fun `verify room database entity operations`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.database.AppDatabase.getInstance(context)
    val userDao = db.userDao()
    val auditDao = db.securityAuditDao()

    // Insert user into Room table
    val userId = userDao.insertUser(
      com.example.data.database.UserAccountEntity(
        fullName = "Jéssica Bernardo Mualaneque",
        phoneNumber = "+258849998888",
        operator = "Vodacom Moçambique",
        pin = "9999",
        role = "ADMIN"
      )
    )
    assertTrue(userId > 0)

    val fetchedUser = userDao.getUserById(userId)
    assertNotNull(fetchedUser)
    assertEquals("Jéssica Bernardo Mualaneque", fetchedUser?.fullName)
    assertEquals("ADMIN", fetchedUser?.role)

    // Insert audit log
    val logId = auditDao.insertLog(
      com.example.data.database.SecurityAuditEntity(
        title = "Teste de Bloqueio Remoto",
        description = "Comando BLOQUEAR executado pelo administrador",
        eventType = "SMS_COMMAND",
        senderNumber = "+258849998888",
        severity = "INFO"
      )
    )
    assertTrue(logId > 0)
  }

  @Test
  fun `verify mozambique gazetteer coverage for all 11 provinces`() {
    val provinces = com.example.security.MozambiqueGazetteer.PROVINCES
    assertEquals(11, provinces.size)

    val provinceIds = provinces.map { it.id }.toSet()
    assertTrue(provinceIds.contains("maputo_cidade"))
    assertTrue(provinceIds.contains("maputo_provincia"))
    assertTrue(provinceIds.contains("gaza"))
    assertTrue(provinceIds.contains("inhambane"))
    assertTrue(provinceIds.contains("sofala"))
    assertTrue(provinceIds.contains("manica"))
    assertTrue(provinceIds.contains("tete"))
    assertTrue(provinceIds.contains("zambezia"))
    assertTrue(provinceIds.contains("nampula"))
    assertTrue(provinceIds.contains("niassa"))
    assertTrue(provinceIds.contains("cabo_delgado"))

    // Test reverse geocoding resolution for Maputo coordinates
    val (nearestMaputo, distMaputo) = com.example.security.MozambiqueGazetteer.findNearestProvince(-25.9692, 32.5732)
    assertEquals("Maputo Cidade", nearestMaputo.name)
    assertTrue(distMaputo < 5.0)

    // Test reverse geocoding resolution for Beira (Sofala) coordinates
    val (nearestBeira, distBeira) = com.example.security.MozambiqueGazetteer.findNearestProvince(-19.8333, 34.8500)
    assertEquals("Sofala", nearestBeira.name)
    assertTrue(distBeira < 5.0)

    // Test distance calculation between Maputo and Beira
    val distanceMaputoBeira = com.example.security.MozambiqueGazetteer.calculateDistanceKm(-25.9692, 32.5732, -19.8333, 34.8500)
    assertTrue(distanceMaputoBeira in 650.0..800.0)
  }

  @Test
  fun `verify secret vault passcode and item management`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vaultRepo = com.example.data.database.VaultRepository(context)

    // Initial state is locked
    assertFalse(vaultRepo.isVaultUnlocked.value)

    // Failed unlock with wrong pin
    val failed = vaultRepo.unlockVault("0000")
    assertFalse(failed)
    assertFalse(vaultRepo.isVaultUnlocked.value)

    // Successful unlock with default device PIN "1234"
    val success = vaultRepo.unlockVault("1234")
    assertTrue(success)
    assertTrue(vaultRepo.isVaultUnlocked.value)

    // Insert hidden photo
    vaultRepo.insertHiddenPhoto(
      title = "Recibo Confidencial",
      subtitle = "Maputo",
      imageUriOrTag = "test_tag",
      fileSizeBytes = 1024 * 500
    )

    // Insert hidden message
    vaultRepo.insertHiddenMessage(
      title = "Código M-Pesa",
      sender = "+258840001122",
      secretMessage = "PIN Secreto: 9812"
    )

    // Insert hidden music
    vaultRepo.insertHiddenMusic(
      title = "Gravação de Voz",
      artistOrDuration = "01:30 min",
      audioNote = "test_audio",
      fileSizeBytes = 1024 * 1024
    )

    // Insert app lock
    vaultRepo.insertOrToggleAppLock(
      appName = "WhatsApp",
      packageName = "com.whatsapp",
      isLocked = true
    )

    // Re-lock vault
    vaultRepo.lockVault()
    assertFalse(vaultRepo.isVaultUnlocked.value)
  }
}


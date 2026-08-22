package com.example.security

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object VaultCryptoManager {

    private const val TAG = "VaultCryptoManager"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "AntiTheftVaultMasterKey_AES256"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // In-memory decrypted temporary cache map: key = item UUID/path, value = DecryptedData
    private val tempDecryptedCache = ConcurrentHashMap<String, ByteArray>()
    private val cacheExpirationMap = ConcurrentHashMap<String, Long>()

    data class EncryptedFileResult(
        val encryptedFileName: String,
        val encryptedFilePath: String,
        val originalFileName: String,
        val fileSizeBytes: Long
    )

    fun getVaultDir(context: Context): File {
        val vaultDir = File(context.filesDir, "cofre")
        if (!vaultDir.exists()) {
            vaultDir.mkdirs()
        }
        // Ensure .nomedia file exists to hide files from Gallery and MediaStore
        val noMediaFile = File(vaultDir, ".nomedia")
        if (!noMediaFile.exists()) {
            try {
                noMediaFile.createNewFile()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating .nomedia: ${e.message}")
            }
        }
        return vaultDir
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts plain byte array and writes to /data/user/0/.../files/cofre/UUID.enc
     */
    fun encryptAndSave(
        context: Context,
        plainBytes: ByteArray,
        originalName: String
    ): EncryptedFileResult {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainBytes)

        val randomId = UUID.randomUUID().toString()
        val encryptedFileName = "$randomId.enc"
        val vaultFile = File(getVaultDir(context), encryptedFileName)

        FileOutputStream(vaultFile).use { fos ->
            fos.write(iv) // Write 12-byte IV first
            fos.write(encryptedBytes)
        }

        return EncryptedFileResult(
            encryptedFileName = encryptedFileName,
            encryptedFilePath = vaultFile.absolutePath,
            originalFileName = originalName,
            fileSizeBytes = plainBytes.size.toLong()
        )
    }

    /**
     * Encrypts a string directly into AES-256 Base64 representation
     */
    fun encryptString(plainText: String): String {
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting string: ${e.message}")
            plainText
        }
    }

    /**
     * Decrypts an AES-256 Base64 string
     */
    fun decryptString(encryptedBase64: String): String {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < GCM_IV_LENGTH) return encryptedBase64

            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)

            val cipherText = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherText.size)

            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(cipherText)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting string: ${e.message}")
            encryptedBase64
        }
    }

    /**
     * Decrypts an encrypted file (UUID.enc) with a 30-second self-destructing memory cache
     */
    fun decryptFileWithAutoExpiringCache(
        context: Context,
        encryptedFileName: String,
        autoPurgeSeconds: Int = 30
    ): ByteArray? {
        // Check if in active memory cache
        val cached = tempDecryptedCache[encryptedFileName]
        val exp = cacheExpirationMap[encryptedFileName] ?: 0L
        if (cached != null && System.currentTimeMillis() < exp) {
            return cached
        }

        val vaultFile = File(getVaultDir(context), encryptedFileName)
        if (!vaultFile.exists()) {
            return null
        }

        try {
            val fileBytes = vaultFile.readBytes()
            if (fileBytes.size <= GCM_IV_LENGTH) return null

            val iv = fileBytes.copyOfRange(0, GCM_IV_LENGTH)
            val cipherBytes = fileBytes.copyOfRange(GCM_IV_LENGTH, fileBytes.size)

            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decrypted = cipher.doFinal(cipherBytes)

            // Store in temp cache
            tempDecryptedCache[encryptedFileName] = decrypted
            val expireTime = System.currentTimeMillis() + (autoPurgeSeconds * 1000L)
            cacheExpirationMap[encryptedFileName] = expireTime

            // Launch coroutine to auto-purge after exact timeout
            CoroutineScope(Dispatchers.Default).launch {
                delay(autoPurgeSeconds * 1000L)
                purgeCachedFile(encryptedFileName)
            }

            return decrypted
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting vault file $encryptedFileName: ${e.message}")
            return null
        }
    }

    /**
     * Immediately destroys cached decrypted data from RAM
     */
    fun purgeCachedFile(encryptedFileName: String) {
        val data = tempDecryptedCache.remove(encryptedFileName)
        cacheExpirationMap.remove(encryptedFileName)
        data?.fill(0) // Overwrite RAM with zeroes for security
    }

    /**
     * Purges all decrypted cache from RAM
     */
    fun purgeAllTempCache() {
        tempDecryptedCache.values.forEach { it.fill(0) }
        tempDecryptedCache.clear()
        cacheExpirationMap.clear()
    }

    /**
     * Delete an encrypted file permanently from storage
     */
    fun deleteEncryptedFile(context: Context, encryptedFileName: String): Boolean {
        purgeCachedFile(encryptedFileName)
        val file = File(getVaultDir(context), encryptedFileName)
        return if (file.exists()) {
            file.delete()
        } else true
    }
}

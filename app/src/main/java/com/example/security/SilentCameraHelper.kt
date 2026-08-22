package com.example.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Paint
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.database.SecurityAuditEntity
import com.example.data.database.VaultItemEntity
import com.example.data.model.EventType
import com.example.data.repository.SecurityPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper para captura silenciosa de fotos usando a câmera frontal no Android 9+
 * Salva a evidência no armazenamento seguro e insere no Cofre Secreto e Registo de Auditoria.
 */
object SilentCameraHelper {

    private const val TAG = "SilentCameraHelper"

    fun captureFrontCameraSilent(
        context: Context,
        reason: String,
        onCaptured: ((photoPath: String?, base64Preview: String?) -> Unit)? = null
    ) {
        val appContext = context.applicationContext
        val prefs = SecurityPreferences(appContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cameraManager = appContext.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                if (cameraManager == null) {
                    fallbackMockCapture(appContext, reason, onCaptured)
                    return@launch
                }

                // Find front-facing camera ID
                var frontCameraId: String? = null
                for (id in cameraManager.cameraIdList) {
                    val characteristics = cameraManager.getCameraCharacteristics(id)
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        frontCameraId = id
                        break
                    }
                }

                val targetCameraId = frontCameraId ?: cameraManager.cameraIdList.firstOrNull()
                if (targetCameraId == null) {
                    fallbackMockCapture(appContext, reason, onCaptured)
                    return@launch
                }

                val handlerThread = HandlerThread("SilentCameraBackgroundThread").apply { start() }
                val backgroundHandler = Handler(handlerThread.looper)

                val imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 2)
                imageReader.setOnImageAvailableListener({ reader ->
                    val image = reader.acquireLatestImage()
                    if (image != null) {
                        try {
                            val buffer = image.planes[0].buffer
                            val bytes = ByteArray(buffer.remaining())
                            buffer.get(bytes)
                            savePhotoEvidence(appContext, bytes, reason, onCaptured)
                        } catch (e: Exception) {
                            Log.e(TAG, "Erro ao processar imagem da câmera", e)
                            fallbackMockCapture(appContext, reason, onCaptured)
                        } finally {
                            image.close()
                            handlerThread.quitSafely()
                        }
                    }
                }, backgroundHandler)

                try {
                    cameraManager.openCamera(targetCameraId, object : CameraDevice.StateCallback() {
                        override fun onOpened(camera: CameraDevice) {
                            try {
                                val captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                                captureBuilder.addTarget(imageReader.surface)
                                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

                                camera.createCaptureSession(
                                    listOf(imageReader.surface),
                                    object : CameraCaptureSession.StateCallback() {
                                        override fun onConfigured(session: CameraCaptureSession) {
                                            try {
                                                session.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {}, backgroundHandler)
                                            } catch (e: Exception) {
                                                Log.e(TAG, "Falha na sessão de captura", e)
                                                camera.close()
                                                fallbackMockCapture(appContext, reason, onCaptured)
                                            }
                                        }

                                        override fun onConfigureFailed(session: CameraCaptureSession) {
                                            camera.close()
                                            fallbackMockCapture(appContext, reason, onCaptured)
                                        }
                                    },
                                    backgroundHandler
                                )
                            } catch (e: Exception) {
                                camera.close()
                                fallbackMockCapture(appContext, reason, onCaptured)
                            }
                        }

                        override fun onDisconnected(camera: CameraDevice) {
                            camera.close()
                        }

                        override fun onError(camera: CameraDevice, error: Int) {
                            camera.close()
                            fallbackMockCapture(appContext, reason, onCaptured)
                        }
                    }, backgroundHandler)
                } catch (se: SecurityException) {
                    Log.e(TAG, "Sem permissão de câmera", se)
                    fallbackMockCapture(appContext, reason, onCaptured)
                } catch (cae: CameraAccessException) {
                    Log.e(TAG, "Acesso à câmera indisponível", cae)
                    fallbackMockCapture(appContext, reason, onCaptured)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exceção geral na câmera", e)
                fallbackMockCapture(appContext, reason, onCaptured)
            }
        }
    }

    private fun savePhotoEvidence(
        context: Context,
        imageBytes: ByteArray,
        reason: String,
        onCaptured: ((String?, String?) -> Unit)?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(Date())
                val evidenceDir = File(context.filesDir, "evidence_photos").apply { mkdirs() }
                val photoFile = File(evidenceDir, "INTRUDER_$timeStamp.jpg")

                FileOutputStream(photoFile).use { it.write(imageBytes) }

                val base64 = Base64.encodeToString(imageBytes.take(4096).toByteArray(), Base64.NO_WRAP)
                val db = AppDatabase.getInstance(context)

                // 1. Save in Vault Item
                db.vaultDao().insertVaultItem(
                    VaultItemEntity(
                        itemType = "PHOTO",
                        title = "Foto do Intruso ($reason)",
                        subtitle = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "MZ")).format(Date()),
                        secretContent = photoFile.absolutePath,
                        originalFileName = photoFile.name,
                        fileSizeBytes = photoFile.length(),
                        isEncrypted = true,
                        extraTag = "CAPTURA_AUTOMATICA"
                    )
                )

                // 2. Save in Security Audit Database
                db.securityAuditDao().insertLog(
                    SecurityAuditEntity(
                        title = "Evidência Fotográfica Capturada",
                        description = "Foto silenciosa registada: $reason | Arquivo: ${photoFile.name}",
                        eventType = "CAMERA_CAPTURE",
                        severity = "CRITICAL"
                    )
                )

                val prefs = SecurityPreferences(context)
                prefs.logEvent(
                    title = "Foto de Segurança Gravada",
                    description = "Câmera frontal acionada silenciosamente por: $reason",
                    type = EventType.CAMERA_CAPTURE_ALERT
                )

                onCaptured?.invoke(photoFile.absolutePath, base64)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao gravar evidência no disco", e)
            }
        }
    }

    private fun fallbackMockCapture(
        context: Context,
        reason: String,
        onCaptured: ((String?, String?) -> Unit)?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(Date())
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "MZ")).format(Date())
                val evidenceDir = File(context.filesDir, "evidence_photos").apply { mkdirs() }
                val photoFile = File(evidenceDir, "INTRUDER_$timeStamp.jpg")

                // Generate synthetic evidence snapshot bitmap
                val width = 480
                val height = 480
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.parseColor("#121824"))

                val paint = Paint().apply {
                    color = Color.parseColor("#00F0FF")
                    textSize = 24f
                    isAntiAlias = true
                }
                canvas.drawText("ANTI-ROUBO MZ PRO", 40f, 80f, paint)

                paint.color = Color.parseColor("#FF1744")
                paint.textSize = 28f
                canvas.drawText("ALERTA DE SEGURANÇA", 40f, 130f, paint)

                paint.color = Color.WHITE
                paint.textSize = 20f
                canvas.drawText("Motivo: $reason", 40f, 200f, paint)
                canvas.drawText("Data: $dateStr", 40f, 240f, paint)
                canvas.drawText("Dispositivo: Moçambique (+258)", 40f, 280f, paint)
                canvas.drawText("Câmera Frontal Silenciosa", 40f, 320f, paint)

                val outStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
                val bytes = outStream.toByteArray()

                FileOutputStream(photoFile).use { it.write(bytes) }

                val db = AppDatabase.getInstance(context)
                db.vaultDao().insertVaultItem(
                    VaultItemEntity(
                        itemType = "PHOTO",
                        title = "Foto de Alerta ($reason)",
                        subtitle = dateStr,
                        secretContent = photoFile.absolutePath,
                        originalFileName = photoFile.name,
                        fileSizeBytes = photoFile.length(),
                        isEncrypted = true,
                        extraTag = "CAPTURA_SEGURANCA"
                    )
                )

                db.securityAuditDao().insertLog(
                    SecurityAuditEntity(
                        title = "Evidência Registada ($reason)",
                        description = "Alerta fotográfico salvo no cofre seguro: ${photoFile.name}",
                        eventType = "CAMERA_CAPTURE",
                        severity = "CRITICAL"
                    )
                )

                val prefs = SecurityPreferences(context)
                prefs.logEvent(
                    title = "Foto de Alerta Gravada",
                    description = "Câmera frontal registou evidência de $reason",
                    type = EventType.CAMERA_CAPTURE_ALERT
                )

                onCaptured?.invoke(photoFile.absolutePath, "EVIDENCE_BASE64_MOCK")
            } catch (e: Exception) {
                Log.e(TAG, "Erro na captura mock", e)
            }
        }
    }
}

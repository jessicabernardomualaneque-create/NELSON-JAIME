package com.example.security

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

object SirenController {

    private var sirenJob: Job? = null
    private var torchJob: Job? = null
    private var isSirenPlaying = false

    fun isPlaying(): Boolean = isSirenPlaying

    fun triggerAlarmAndFlash(context: Context, durationSeconds: Int = 10, onFinished: (() -> Unit)? = null) {
        stopAlarm(context)
        isSirenPlaying = true

        val scope = CoroutineScope(Dispatchers.Default)

        // 1. Play synthesized siren tone
        sirenJob = scope.launch {
            try {
                playSynthesizedSiren(context, durationSeconds)
            } finally {
                isSirenPlaying = false
                onFinished?.invoke()
            }
        }

        // 2. Pulse flashlight (Strobe effect)
        torchJob = scope.launch {
            pulseFlashlight(context, durationSeconds)
        }

        // 3. Trigger emergency vibration pattern
        triggerVibration(context, durationSeconds)
    }

    fun stopAlarm(context: Context) {
        isSirenPlaying = false
        sirenJob?.cancel()
        sirenJob = null
        torchJob?.cancel()
        torchJob = null
        turnOffFlashlight(context)
        stopVibration(context)
    }

    private suspend fun playSynthesizedSiren(context: Context, durationSeconds: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val originalVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 5
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 15

        // Maximize volume for alarm
        try {
            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val sampleRate = 44100
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = if (minBufferSize > 0) minBufferSize else 4096

        val audioTrackBuilder = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioTrackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
        }

        val audioTrack = audioTrackBuilder.build()

        try {
            audioTrack.play()
            val startTime = System.currentTimeMillis()
            val endTime = startTime + (durationSeconds * 1000L)
            var currentFreq = 800.0
            var ascending = true
            var phase = 0.0

            val samples = ShortArray(bufferSize)

            while (System.currentTimeMillis() < endTime && isSirenPlaying) {
                for (i in samples.indices) {
                    val angle = 2.0 * Math.PI * currentFreq / sampleRate
                    phase += angle
                    if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                    samples[i] = (sin(phase) * 32767).toInt().toShort()

                    // Sweep frequency 800Hz <-> 1800Hz
                    if (ascending) {
                        currentFreq += 1.2
                        if (currentFreq >= 1800.0) ascending = false
                    } else {
                        currentFreq -= 1.2
                        if (currentFreq <= 800.0) ascending = true
                    }
                }
                audioTrack.write(samples, 0, samples.size)
                delay(1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                audioTrack.stop()
                audioTrack.release()
                // Restore original volume
                audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun pulseFlashlight(context: Context, durationSeconds: Int) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
        var cameraId: String? = null

        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (flashAvailable && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (cameraId == null) return

        val endTime = System.currentTimeMillis() + (durationSeconds * 1000L)
        var torchState = false

        try {
            while (System.currentTimeMillis() < endTime && isSirenPlaying) {
                torchState = !torchState
                try {
                    cameraManager.setTorchMode(cameraId, torchState)
                } catch (e: Exception) {
                    // Ignore if camera in use
                }
                delay(150)
            }
        } finally {
            try {
                cameraManager.setTorchMode(cameraId, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun turnOffFlashlight(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (flashAvailable) {
                    try {
                        cameraManager.setTorchMode(id, false)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerVibration(context: Context, durationSeconds: Int) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val timings = longArrayOf(0, 400, 200, 400, 200, 800)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

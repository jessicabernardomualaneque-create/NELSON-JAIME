package com.example.security

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.database.SecurityAuditEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

object SilentAudioRecorderHelper {

    private const val TAG = "SilentAudioRecorder"

    fun record5SecondsSilentAudio(context: Context, reason: String) {
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val audioDir = File(appContext.filesDir, "security_audio")
                if (!audioDir.exists()) audioDir.mkdirs()

                val audioFile = File(audioDir, "EVIDENCE_AUDIO_${System.currentTimeMillis()}.m4a")

                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(appContext)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }

                try {
                    recorder.apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setAudioEncodingBitRate(128000)
                        setAudioSamplingRate(44100)
                        setOutputFile(audioFile.absolutePath)
                        prepare()
                        start()
                    }

                    // Record for 5 seconds
                    delay(5000)

                    try {
                        recorder.stop()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping recorder: ${e.message}")
                    } finally {
                        recorder.release()
                    }

                    val db = AppDatabase.getInstance(appContext)
                    db.securityAuditDao().insertLog(
                        SecurityAuditEntity(
                            title = "Áudio de 5s Capturado (Violação de PIN)",
                            description = "Gravação silenciosa de áudio de intruso [$reason]. Ficheiro: ${audioFile.name}",
                            eventType = "AUDIO_EVIDENCE",
                            severity = "CRITICAL"
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Recording error: ${e.message}")
                    try { recorder.release() } catch (ignored: Exception) {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Silent audio capture exception: ${e.message}")
            }
        }
    }
}

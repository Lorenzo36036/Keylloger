import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class SocketServer(private val port: Int) {
    private var serverSocket: ServerSocket? = null

    fun start() {
        thread { // Iniciar en un hilo separado para evitar bloquear el hilo principal
            try {
                serverSocket = ServerSocket(port)
                println("Servidor escuchando en el puerto $port")

                while (true) {
                    // Aceptar conexiones de clientes
                    val clientSocket = serverSocket!!.accept()
                    println("Cliente conectado desde ${clientSocket.inetAddress}")
                    // Manejar al cliente en un hilo separado
                    thread { ClientHandler(clientSocket).run() }
                }
            } catch (e: IOException) {
                println("Error en el servidor: ${e.message}")
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        try {
            serverSocket?.close()
            println("Servidor detenido.")
        } catch (e: IOException) {
            println("Error al cerrar el servidor: ${e.message}")
        }
    }

    class ClientHandler(private val client: Socket) {
        fun run() {
            try {
                client.getInputStream().bufferedReader().use { input ->
                    client.getOutputStream().bufferedWriter().use { output ->
                        while (true) {
                            // Leer mensaje del cliente
                            val message = input.readLine()
                            if (message == null || message.equals("EXIT", ignoreCase = true)) {
                                println("Cliente desconectado: ${client.inetAddress}")
                                break
                            }
                            println("Mensaje recibido del cliente: $message")

                            // Simulamos una operación (por ejemplo, sumas)
                            val result = processMessage(message)
                            output.write("$result\n")
                            output.flush()
                        }
                    }
                }
            } catch (e: IOException) {
                println("Error al manejar la conexión del cliente: ${e.message}")
            } finally {
                try {
                    client.close()
                } catch (e: IOException) {
                    println("Error al cerrar la conexión del cliente: ${e.message}")
                }
            }
        }

        // Lógica simple de procesamiento de la operación
        private fun processMessage(message: String): String {
            val parts = message.split(" ")
            if (parts.size == 3) {
                val num1 = parts[0].toIntOrNull()
                val num2 = parts[1].toIntOrNull()
                val operation = parts[2]

                if (num1 != null && num2 != null) {
                    return when (operation) {
                        "add" -> (num1 + num2).toString()
                        "sub" -> (num1 - num2).toString()
                        "multi" -> (num1 * num2).toString()
                        "div" -> if (num2 != 0) (num1 / num2).toString() else "Error: División por cero"
                        else -> "Error: Operación desconocida"
                    }
                }
            }
            return "Error: Formato incorrecto"
        }
    }
}

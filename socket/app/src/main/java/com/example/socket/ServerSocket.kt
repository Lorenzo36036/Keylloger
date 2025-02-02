import java.net.ServerSocket
import java.net.Socket

class MyServerSocket() {



    fun startServer() {
     val serverPort = 12345
    val serverSocket = ServerSocket(serverPort) // Crear el ServerSocket
    println("Server listening on port $serverPort")
    while (true) {
        // Aceptar una conexión entrante
        val clientSocket: Socket = serverSocket.accept()
        println("Connection from ${clientSocket.inetAddress}")

        // Manejar la conexión del cliente en un hilo separado
        Thread {
            handleClient(clientSocket)
        }.start()
    }
}

fun handleClient(clientSocket: Socket) {
    try {
        val inputStream = clientSocket.getInputStream()
        val buffer = ByteArray(1024) // Buffer para almacenar los datos recibidos

        while (true) {
            val bytesRead = inputStream.read(buffer) // Leer datos del cliente
            if (bytesRead == -1) {
                break // Si no hay más datos, salir del bucle
            }

            // Convertir los bytes a una cadena y mostrarlos
            val data = String(buffer, 0, bytesRead)
            println("Received: $data")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    } finally {
        // Cerrar el socket del cliente
        clientSocket.close()
        println("Connection closed")
    }
}

}

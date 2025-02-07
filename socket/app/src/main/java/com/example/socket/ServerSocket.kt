import android.os.Handler
import android.os.Message
import java.net.ServerSocket
import java.net.Socket

class MyServerSocket(private val handler: Handler) {

    fun startServer() {
        val serverPort = 12345
        val serverSocket = ServerSocket(serverPort)
        println("Server listening on port $serverPort")
        while (true) {
            val clientSocket: Socket = serverSocket.accept()
            println("Connection from ${clientSocket.inetAddress}")
            Thread {
                handleClient(clientSocket)
            }.start()
        }
    }

    fun handleClient(clientSocket: Socket) {
        try {
            val inputStream = clientSocket.getInputStream()
            val buffer = ByteArray(1024)

            while (true) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) {
                    break
                }

                val data = String(buffer, 0, bytesRead)
                println("Received: $data")

                // Enviar los datos al Handler para actualizar el TextView
                val message = Message.obtain()
                message.obj = data
                handler.sendMessage(message)
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        } finally {
            clientSocket.close()
            println("Connection closed")
        }
    }
}
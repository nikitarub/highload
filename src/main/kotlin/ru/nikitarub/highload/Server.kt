package ru.nikitarub.highload

import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import ru.nikitarub.highload.exceptions.Forbidden
import ru.nikitarub.highload.exceptions.MethodNotAllowed
import ru.nikitarub.highload.exceptions.NotFound
import ru.nikitarub.highload.exceptions.ServerException
import ru.nikitarub.highload.myhttp.Request
import ru.nikitarub.highload.myhttp.RequestMethod
import ru.nikitarub.highload.myhttp.Response
import ru.nikitarub.highload.utils.HttpdConfig
import java.io.BufferedOutputStream
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset

var rootFile = File("./")

fun main(args: Array<String>) {
    val config = HttpdConfig.readDefaultConfig()

    var threadCount = config["cpu_limit"]!!.toInt()
    val threadExecutor = newFixedThreadPoolContext(threadCount, "highload")

    var port = config["listen"]!!.toInt()
    rootFile = File(config["document_root"]?:"/var/www/html")

    println("Start server on port: $port and static folder: ${rootFile.absoluteFile}")

    val server = ServerSocket(port)
    while (true) {
        val socket = server.accept()
        launch(threadExecutor) {
            socket.use { socket ->
                processRequest(socket)
            }
            socket.close()
        }
    }
}

fun processRequest(socket: Socket) {
    val request = Request(socket.getInputStream())

    val response = try {
        getResponse(request)
    } catch (serverException: ServerException) {
        Response(serverException)
    }

    BufferedOutputStream(socket.getOutputStream()).use { outputStream ->
        outputStream.write(response.getHeaders().toByteArray(Charset.forName("UTF-8")))
        if (request.requestMethod != RequestMethod.HEAD) {
            response.file?.inputStream().use { it?.copyTo(outputStream) }
        }
    }
}

fun getResponse(request: Request): Response {
    if (request.requestMethod == RequestMethod.UNKNOWN) {
        throw MethodNotAllowed()
    }

    if (request.path.contains("../")) {
        throw Forbidden()
    }

    val file = File(rootFile, request.path)
    println(file.absoluteFile)
    if (!file.exists()) {
        if (request.isIndex) {
            throw Forbidden()
        }
        throw NotFound()
    }

    return Response(200, "OK", file)
}

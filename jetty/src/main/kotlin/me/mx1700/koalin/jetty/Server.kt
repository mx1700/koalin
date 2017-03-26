package me.mx1700.koalin.jetty

import me.mx1700.koalin.servlet.*
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

fun Application.listen(port: Int = 9000) {
    val server = Server(port);
    server.handler = object : AbstractHandler() {
        override fun handle(target: String?, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {
            val ctx = Context(this@listen, request, response)
            callback(ctx)
            baseRequest.isHandled = true
        }
    }
    server.start();
    server.join();
}
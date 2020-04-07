package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.extensions.json.toInstance
import com.github.squirrelgrip.scientist4k.configuration.SslConfiguration
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import java.io.File
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ControlHandler : AbstractHandler() {
    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        println("${request.method} ${request.requestURL}")
        val out = response.writer
        when (target) {
            "/candidate" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                out.println("<h1>Hello</h1>")
            }
            "/ok" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                out.println("<h1>Hello</h1>")
            }
            "/status" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                out.println("<h1>Hello</h1>")
            }
            "/contentType" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                out.println("<h1>Hello</h1>")
            }
            "/cookie" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                response.addCookie(Cookie("name", "value"))
                out.println("<h1>Hello</h1>")
            }
            "/addcookie" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                response.addCookie(Cookie("name", "value"))
                out.println("<h1>Hello</h1>")
            }
            "/alteredcookie" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                response.addCookie(Cookie("name", "value"))
                out.println("<h1>Hello</h1>")
            }
            "/removedcookie" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                val cookie = Cookie("name", "value")
                response.addCookie(cookie)
                out.println("<h1>Hello</h1>")
            }
            "/redirect" -> {
                response.sendRedirect("/ok")
            }
            else -> {
                response.status = HttpServletResponse.SC_NOT_FOUND
            }
        }
        baseRequest.isHandled = true
    }
}

fun main() {
    val sslConfiguration = File("config.json").toInstance<SslConfiguration>()
    val server = SecuredServer(9001, 9002, ControlHandler(), sslConfiguration)
    server.start();
    server.join();
}
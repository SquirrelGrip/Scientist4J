package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.configuration.ConnectorConfiguration
import com.github.squirrelgrip.scientist4k.configuration.ServerConfiguration
import com.github.squirrelgrip.scientist4k.configuration.SslConfiguration
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ControlHandler : AbstractHandler() {
    companion object {
        val serverConfiguration = ServerConfiguration(
                listOf(
                        ConnectorConfiguration(9001),
                        ConnectorConfiguration(9002,
                                SslConfiguration("target/certs/keystore.jks",
                                        "password",
                                        "JKS",
                                        "target/certs/keystore.jks",
                                        "password",
                                        "JKS",
                                        "TLSv1.2"
                                )
                        )
                )
        )
    }

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
    val server = SecuredServer(ControlHandler.serverConfiguration, CandidateHandler())
    server.start()
    server.join()
}
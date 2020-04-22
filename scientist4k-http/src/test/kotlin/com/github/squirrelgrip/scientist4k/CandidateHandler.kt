package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.configuration.ConnectorConfiguration
import com.github.squirrelgrip.scientist4k.configuration.ServerConfiguration
import com.github.squirrelgrip.scientist4k.configuration.SslConfiguration
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class CandidateHandler : AbstractHandler() {
    companion object {
        val serverConfiguration = ServerConfiguration(
                listOf(
                        ConnectorConfiguration(9011),
                        ConnectorConfiguration(9012,
                                SslConfiguration(
                                        "target/certs/keystore.jks",
                                        "pass:password",
                                        "JKS",
                                        "target/certs/keystore.jks",
                                        "pass:password",
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
//        println("${request.method} ${request.requestURL}")
        val out = response.writer
        when (target) {
            "/candidate" -> {
                response.contentType = "text/plain;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                out.println("Candidate")
            }
            "/ok" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                out.println("<h1>OK</h1>")
            }
            "/differentContent" -> {
                response.contentType = "text/plain;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                out.println("Candidate Content")
            }
            "/mappedCandidate" -> {
                response.contentType = "text/plain;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                out.println("mapped")
            }
            "/status" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_CREATED
                out.println("<h1>status</h1>")
            }
            "/contentType" -> {
                response.contentType = "text/html"
                response.status = HttpServletResponse.SC_CREATED
                out.println("<h1>content type</h1>")
            }
            "/cookie" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                response.addCookie(Cookie("name", "value"))
                out.println("<h1>Cookie</h1>")
            }
            "/addcookie" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                response.addCookie(Cookie("name", "value"))
                response.addCookie(Cookie("add", "value"))
                out.println("<h1>Hello</h1>")
            }
            "/alteredcookie" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
                response.addCookie(Cookie("name", "different_value"))
                out.println("<h1>Hello</h1>")
            }
            "/removedcookie" -> {
                response.contentType = "text/html;charset=utf-8"
                response.status = HttpServletResponse.SC_OK
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
    val server = SecuredServer(CandidateHandler.serverConfiguration, CandidateHandler())
    server.start()
    server.join()
}

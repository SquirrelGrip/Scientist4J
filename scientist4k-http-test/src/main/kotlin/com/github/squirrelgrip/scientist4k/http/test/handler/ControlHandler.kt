package com.github.squirrelgrip.scientist4k.http.test.handler

import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.scientist4k.http.core.configuration.ConnectorConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.ServerConfiguration
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import com.google.common.net.MediaType
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ControlHandler : AbstractHandler() {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ControlHandler::class.java)

        val serverConfiguration = ServerConfiguration(
                listOf(
                        ConnectorConfiguration(9001),
                        ConnectorConfiguration(9002,
                                Main.sslConfiguration
                        )
                )
        )

        fun handleRequest(request: HttpServletRequest, response: HttpServletResponse, target: String) {
            LOGGER.info("ControlHandler received request: ${request.method} ${request.requestURL}")
            val out = response.writer
            when (target) {
                "/control" -> {
                    response.contentType = "text/plain;charset=utf-8"
                    response.status = HttpServletResponse.SC_OK
                    out.println("Control")
                }
                "/ok" -> {
                    response.contentType = "text/html;charset=utf-8"
                    response.status = HttpServletResponse.SC_OK
                    out.println("<h1>OK</h1>")
                }
                "/differentContent" -> {
                    response.contentType = "text/plain;charset=utf-8"
                    response.status = HttpServletResponse.SC_OK
                    out.println("Control Content")
                }
                "/mappedControl" -> {
                    response.contentType = "text/plain;charset=utf-8"
                    response.status = HttpServletResponse.SC_OK
                    out.println("mapped")
                }
                "/status" -> {
                    response.contentType = "text/html;charset=utf-8"
                    response.status = HttpServletResponse.SC_OK
                    out.println("<h1>status</h1>")
                }
                "/contentType" -> {
                    response.contentType = "text/html;charset=utf-8"
                    response.status = HttpServletResponse.SC_OK
                    out.println("<h1>content type</h1>")
                }
                "/cookie" -> {
                    response.contentType = "text/html;charset=utf-8"
                    response.status = HttpServletResponse.SC_OK
                    response.addCookie(Cookie("name", "value"))
                    out.println("<h1>cookie</h1>")
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
                "/json" -> {
                    response.contentType = MediaType.JSON_UTF_8.toString()
                    response.status = HttpServletResponse.SC_OK
                    out.println(mapOf("1" to "AAA", "2" to listOf("BBB", "CCC"), "3" to mapOf("4" to listOf("DDD", "EEE"))).toJson())
                }
                "/jsonDifferent" -> {
                    response.contentType = MediaType.JSON_UTF_8.toString()
                    response.status = HttpServletResponse.SC_OK
                    out.println(mapOf("1" to "AAA", "2" to listOf("BBB", "CCC"), "3" to mapOf("4" to listOf("DDD", "EEE"))).toJson())
                }
                else -> {
                    response.status = HttpServletResponse.SC_NOT_FOUND
                }
            }
        }
    }

    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        handleRequest(request, response, target)
        baseRequest.isHandled = true
    }

}

fun main() {
    val server = SecuredServer(ControlHandler.serverConfiguration, CandidateHandler())
    server.start()
    server.join()
}
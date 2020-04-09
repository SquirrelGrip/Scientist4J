package com.github.squirrelgrip.scientist4k.factory

import com.github.squirrelgrip.scientist4k.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.model.ExperimentResponse
import org.apache.http.HttpResponse
import org.apache.http.client.CookieStore
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.entity.ContentType
import org.apache.http.entity.InputStreamEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.protocol.HTTP.CONTENT_LEN
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

class RequestFactory(
        val endPointConfig: EndPointConfiguration,
        val cookieStoreAttributeName: String
) {
    fun create(
            request: HttpServletRequest
    ): () -> ExperimentResponse {
        return {
            val session: HttpSession? = getSession(request)
            val cookieStore: CookieStore? = session?.getAttribute(cookieStoreAttributeName) as CookieStore
            createHttpClient(cookieStore).use {
                val url = buildUrl(request)
                val httpUriRequest: HttpUriRequest = createRequest(request, url)

                val responseHandler = ResponseHandler { response ->
                    ExperimentResponse(
                            response.statusLine,
                            response.allHeaders,
                            response.entity
                    )
                }
                val response = it.execute(httpUriRequest, responseHandler)
                if (cookieStore != null) {
                    getSession(request)?.setAttribute(cookieStoreAttributeName, cookieStore)
                }
                response
            }
        }
    }

    private fun buildUrl(request: HttpServletRequest): String {
        val url = StringBuffer("${endPointConfig.url}${request.requestURI}")
        if (!request.queryString.isNullOrEmpty()) {
            url.append("?${request.queryString}")
        }
        return url.toString()
    }

    private fun createRequest(request: HttpServletRequest, url: String): HttpUriRequest {
        return RequestBuilder.create(request.method).apply {
            setUri(url)
            request.headerNames.iterator().forEach { headerName ->
                if (headerName != CONTENT_LEN) {
                    setHeader(headerName, request.getHeader(headerName))
                }
            }
            entity = InputStreamEntity(request.inputStream, request.contentLengthLong, ContentType.getByMimeType(request.contentType))
        }.build()
    }

    private fun createHttpClient(cookieStore: CookieStore?): CloseableHttpClient {
        val clientBuilder = HttpClients.custom()
        if (cookieStore != null) {
            clientBuilder.setDefaultCookieStore(cookieStore)
        }
        if (endPointConfig.sslConfiguration != null) {
            clientBuilder.setSSLSocketFactory(
                    SSLConnectionSocketFactory(
                            endPointConfig.sslConfiguration.sslContext(),
                            SSLConnectionSocketFactory.getDefaultHostnameVerifier()
                    )
            )
        }
        return clientBuilder.build()
    }

    private fun getSession(request: HttpServletRequest): HttpSession? {
        val session = request.getSession(false)
        if (session != null) {
            return session.apply {
                if (getAttribute(cookieStoreAttributeName) == null) {
                    setAttribute(cookieStoreAttributeName, BasicCookieStore())
                }
            }
        }
        return null
    }

}

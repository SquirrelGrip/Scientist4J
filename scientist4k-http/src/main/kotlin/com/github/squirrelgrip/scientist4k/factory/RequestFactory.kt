package com.github.squirrelgrip.scientist4k.factory

import com.github.squirrelgrip.scientist4k.configuration.EndPointConfiguration
import org.apache.http.HttpResponse
import org.apache.http.client.CookieStore
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.entity.ContentType
import org.apache.http.entity.InputStreamEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import javax.servlet.http.HttpServletRequest

class RequestFactory(
        val endPointConfig: EndPointConfiguration,
        val cookieStoreAttributeName: String
) {
    fun create(
            request: HttpServletRequest
    ): () -> HttpResponse {
        return {
            val session = getSession(request)
            val cookieStore = session.getAttribute(cookieStoreAttributeName) as CookieStore

            createHttpClient(cookieStore).use {
                val url = buildUrl(request)
                val httpUriRequest: HttpUriRequest = createRequest(request, url)
                val response = it.execute(httpUriRequest)
                session.setAttribute(cookieStoreAttributeName, cookieStore)
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
                setHeader(headerName, request.getHeader(headerName))
            }
            entity = InputStreamEntity(request.inputStream, request.contentLengthLong, ContentType.getByMimeType(request.contentType))
        }.build()
    }

    private fun createHttpClient(cookieStore: CookieStore): CloseableHttpClient {
        val clientBuilder = HttpClients.custom().setDefaultCookieStore(cookieStore)
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

    private fun getSession(request: HttpServletRequest) =
            request.session.apply {
                if (getAttribute(cookieStoreAttributeName) == null) {
                    setAttribute(cookieStoreAttributeName, BasicCookieStore())
                }
            }

}

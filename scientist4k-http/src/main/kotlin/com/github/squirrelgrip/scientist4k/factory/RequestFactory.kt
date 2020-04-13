package com.github.squirrelgrip.scientist4k.factory

import com.github.squirrelgrip.scientist4k.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.model.ExperimentRequest
import com.github.squirrelgrip.scientist4k.model.ExperimentResponse
import org.apache.http.HttpVersion.HTTP_1_1
import org.apache.http.client.CookieStore
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.cookie.Cookie
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.protocol.HTTP.CONTENT_LEN
import javax.servlet.http.HttpSession

class RequestFactory(
        val endPointConfig: EndPointConfiguration,
        val cookieStoreAttributeName: String
) {
    fun create(
            request: ExperimentRequest
    ): () -> ExperimentResponse {
        return {
            val cookieStore: CookieStore = getCookieStore(request)
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
                cookieStore.cookies.forEach {cookie ->
                    println("${cookie.name}=${cookie.value}")
                }
                getSession(request).setAttribute(cookieStoreAttributeName, cookieStore)
                response
            }
        }
    }

    private fun getCookieStore(request: ExperimentRequest): CookieStore =
        getSession(request).getAttribute(cookieStoreAttributeName) as CookieStore

    private fun buildUrl(request: ExperimentRequest): String = "${endPointConfig.url}${request.url}"

    private fun createRequest(request: ExperimentRequest, url: String): HttpUriRequest =
            RequestBuilder.create(request.method).apply {
                setUri(url)
                version = HTTP_1_1
                request.headers.forEach { (headerName, headerValue) ->
                    if (headerName != CONTENT_LEN) {
                        setHeader(headerName, headerValue)
                    }
                }
                entity = ByteArrayEntity(request.body, ContentType.getByMimeType(request.contentType))
            }.build()

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

    private fun getSession(request: ExperimentRequest): HttpSession =
            request.session.apply {
                if (this.getAttribute(cookieStoreAttributeName) == null) {
                    val cookieStore = BasicCookieStore()
                    request.cookies.forEach {
                        val cookie = BasicClientCookie(it.name, it.value)
                        cookie.domain = it.domain
                        cookie.path = it.path
                        cookieStore.cookies.add(cookie)
                    }
                    this.setAttribute(cookieStoreAttributeName, cookieStore)
                }
            }

}

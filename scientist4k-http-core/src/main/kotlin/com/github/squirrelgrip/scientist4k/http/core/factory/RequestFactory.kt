package com.github.squirrelgrip.scientist4k.http.core.factory

import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentRequest
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import org.apache.http.HttpVersion.HTTP_1_1
import org.apache.http.client.CookieStore
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.protocol.HTTP.CONTENT_LEN
import org.apache.http.protocol.HTTP.TARGET_HOST
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

class RequestFactory (
        val endPointConfig: EndPointConfiguration,
        val cookieStoreAttributeName: String,
        val mappingConfiguration: List<MappingConfiguration> = emptyList()
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(RequestFactory::class.java)
    }

    val sessions: MutableMap<String, MutableMap<String, CookieStore>> = mutableMapOf()

    fun create(
            request: ExperimentRequest
    ): () -> ExperimentResponse {
        return {
            val cookieStore: CookieStore = getCookieStore(request)
            createHttpClient(cookieStore).use {
                val url = buildUrl(request)
                val httpUriRequest: HttpUriRequest = createRequest(request, url)

                val responseHandler = ResponseHandler { response ->
                    LOGGER.debug("Response received for request {}", url)
                    ExperimentResponse(
                            response.statusLine,
                            response.allHeaders,
                            response.entity
                    )
                }
                val response = it.execute(httpUriRequest, responseHandler)
                if (LOGGER.isDebugEnabled) {
                    LOGGER.debug("${request.url}=>${response.status}")
                    cookieStore.cookies.forEach { cookie ->
                        LOGGER.debug("${cookie.name}=${cookie.value}")
                    }
                }
                setCookieStore(request, cookieStore)
                response
            }
        }
    }

    private fun setCookieStore(request: ExperimentRequest, cookieStore: CookieStore) {
        getSession(request)[cookieStoreAttributeName] = cookieStore
    }

    private fun getCookieStore(request: ExperimentRequest): CookieStore =
            getSession(request)[cookieStoreAttributeName]!!

    private fun buildUrl(request: ExperimentRequest): String {
        var url = request.url
        val replacements = mappingConfiguration.filter {
            it.matches(url)
        }
        replacements.forEach {
            url = it.replace(url)
        }
        return "${endPointConfig.url}$url"
    }

    private fun createRequest(request: ExperimentRequest, targetUrl: String): HttpUriRequest =
            RequestBuilder.create(request.method).apply {
                setUri(targetUrl)
                version = HTTP_1_1
                request.headers.forEach { (headerName, headerValue) ->
                    when (headerName) {
                        TARGET_HOST -> {
                            val url = URL(targetUrl)
                            setHeader(TARGET_HOST, "${url.host}:${url.port}")
                        }
                        CONTENT_LEN -> {}
                        else -> {
                            setHeader(headerName, headerValue)
                        }
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

    private fun getSession(request: ExperimentRequest): MutableMap<String, CookieStore> {
        synchronized(request.session) {
            var map = sessions[request.session.id]
            if (map == null) {
                map = mutableMapOf(cookieStoreAttributeName to BasicCookieStore())
                sessions[request.session.id] = map
            }
            return map
        }
    }

}

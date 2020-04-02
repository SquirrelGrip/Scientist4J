package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.configuration.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.configuration.SslConfiguration
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.model.HttpResponseComparator
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
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
import javax.servlet.http.HttpServletResponse

class HttpExperiment(
        name: String,
        raiseOnMismatch: Boolean,
        metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        context: Map<String, Any> = emptyMap(),
        comparator: ExperimentComparator<HttpResponse> = HttpResponseComparator(),
        sampleFactory: SampleFactory = SampleFactory(),
        val controlConfig: EndPointConfiguration,
        val candidateConfig: EndPointConfiguration
) : Experiment<HttpResponse>(
        name,
        raiseOnMismatch,
        metrics,
        context,
        comparator,
        sampleFactory
) {
    fun run(
            inboundRequest: HttpServletRequest,
            inboundResponse: HttpServletResponse
    ) {
        try {
            val sample = sampleFactory.create()
            sample.add(inboundRequest.requestURI)
            val controlResponse = if (controlConfig.allowedMethods.contains("*") or controlConfig.allowedMethods.contains(inboundRequest.method)) {
                run(createControlRequest(inboundRequest), createCandidateRequest(inboundRequest), sample)
            } else {
                createControlRequest(inboundRequest).invoke()
            }
            processResponse(inboundResponse, controlResponse)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        inboundResponse.flushBuffer()
    }

    private fun processResponse(
            inboundResponse: HttpServletResponse,
            controlResponse: HttpResponse?
    ) {
        if (controlResponse != null) {
            val bytes = controlResponse.entity.content.readBytes()
            inboundResponse.status = controlResponse.statusLine.statusCode
            controlResponse.allHeaders.forEach {
                inboundResponse.addHeader(it.name, it.value)
            }
            inboundResponse.outputStream.write(bytes)
        } else {
            inboundResponse.status = 500
            inboundResponse.writer.println("Something went wrong with experiment")
        }
    }

    private fun createControlRequest(request: HttpServletRequest): () -> HttpResponse {
        return createRequest(request, controlConfig.url, "CONTROL_COOKIE_STORE", controlConfig.sslConfiguration)
    }

    private fun createCandidateRequest(request: HttpServletRequest): () -> HttpResponse {
        return createRequest(request, candidateConfig.url, "CANDIDATE_COOKIE_STORE", candidateConfig.sslConfiguration)
    }

    private fun createRequest(
            request: HttpServletRequest,
            baseUrl: String,
            cookieStoreAttribute: String,
            sslConfiguration: SslConfiguration?
    ): () -> HttpResponse {
        return {
            val session = getSession(request)
            val cookieStore = session.getAttribute(cookieStoreAttribute) as CookieStore

            createHttpClient(cookieStore, sslConfiguration).use {
                val url = getUrl(baseUrl, request)
                val httpUriRequest: HttpUriRequest = createRequest(request, url)
                val httpResponse = it.execute(httpUriRequest)
                session.setAttribute(cookieStoreAttribute, cookieStore)
                httpResponse
            }
        }
    }

    private fun getUrl(baseUrl: String, request: HttpServletRequest): String {
        val url = StringBuffer("${baseUrl}${request.requestURI}")
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

    private fun createHttpClient(cookieStore: CookieStore, sslConfiguration: SslConfiguration?): CloseableHttpClient {
        val clientBuilder = HttpClients.custom().setDefaultCookieStore(cookieStore)
        if (sslConfiguration != null) {
            clientBuilder.setSSLSocketFactory(
                    SSLConnectionSocketFactory(
                            sslConfiguration.sslContext(),
                            SSLConnectionSocketFactory.getDefaultHostnameVerifier()
                    )
            )
        }
        return clientBuilder.build()
    }

    private fun getSession(request: HttpServletRequest) =
            request.getSession(true).apply {
                if (isNew) {
                    setAttribute("CONTROL_COOKIE_STORE", BasicCookieStore())
                    setAttribute("CANDIDATE_COOKIE_STORE", BasicCookieStore())
                }
            }
}



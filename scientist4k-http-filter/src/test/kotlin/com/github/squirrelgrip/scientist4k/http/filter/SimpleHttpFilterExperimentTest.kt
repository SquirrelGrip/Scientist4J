package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.cheti.Cheti
import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.http.core.consumer.FileConsumer
import com.github.squirrelgrip.scientist4k.http.core.extension.failureReasons
import com.github.squirrelgrip.scientist4k.http.core.extension.matches
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import com.github.squirrelgrip.scientist4k.http.test.handler.CandidateHandler
import com.github.squirrelgrip.scientist4k.http.test.servlet.ControlServlet
import com.google.common.eventbus.Subscribe
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.servlet.DispatcherType

internal class SimpleHttpFilterExperimentTest {

    companion object {
        private val HTTP_CONTROL_URL = "http://localhost:9003"
        private val HTTPS_CONTROL_URL = "https://localhost:9004"

        val filterExperimentConfiguration = File("filter-experiment-config.json").toInstance<FilterExperimentConfiguration>()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val chetiConfiguration = File("../certs/cheti.json")
            val cheti = Cheti(chetiConfiguration)
            cheti.execute()

            val holder: FilterHolder = FilterHolder(ConfigurableExperimentFilter::class.java)
            holder.name = "Experiment Filter"
            holder.initParameters = mapOf(
                    "config" to "filter-experiment-config.json"
            )

            val context = ServletContextHandler(ServletContextHandler.SESSIONS).apply {
                contextPath = "/"
                addServlet(ControlServlet::class.java, "/")
                addFilter(holder, "/*", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST))
            }
            val controlServer = SecuredServer(ControlServlet.serverConfiguration, context)
            controlServer.start()

            val candidateServer = SecuredServer(CandidateHandler.serverConfiguration, CandidateHandler())
            candidateServer.start()
        }

        fun isRunning(url: String): Boolean {
            val sslConfiguration = filterExperimentConfiguration.detour.sslConfiguration
            val httpClient = HttpClients.custom().setSSLSocketFactory(
                    SSLConnectionSocketFactory(
                            sslConfiguration!!.sslContext(),
                            SSLConnectionSocketFactory.getDefaultHostnameVerifier()
                    )
            ).build()
            val request = RequestBuilder.get(url).build()
            val response = httpClient.execute(request)
            return response.statusLine.statusCode == 200
        }
    }

    init {
        AbstractExperiment.DEFAULT_EVENT_BUS.register(this)
        val file = File(File(System.getenv("user.dir"), ".."), "report")
        AbstractExperiment.DEFAULT_EVENT_BUS.register(FileConsumer(file))
    }

    val actualExperimentResult: MutableList<HttpExperimentResult> = mutableListOf()

    @Subscribe
    fun receiveResult(experimentResult: HttpExperimentResult) {
        actualExperimentResult.add(experimentResult)
    }

    @BeforeEach
    fun beforeEach() {
        actualExperimentResult.clear()
    }

    private fun getResult(uri: String): HttpExperimentResult? {
        return actualExperimentResult.firstOrNull {
            it.request.url == uri
        }
    }

    private fun awaitResult(url: String): HttpExperimentResult {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until {
            getResult(url) != null
        }
        val result = getResult(url)
        Assertions.assertNotNull(result)
        return result!!
    }

    @Test
    fun resultsAreSame() {
        assertThat(isRunning("$HTTPS_CONTROL_URL/ok")).isTrue()

        val result = awaitResult("/ok")
        assertThat(result.matches()).isTrue()
        assertThat(result.failureReasons()).isEmpty()
    }

    @Test
    fun resultsAreDifferent() {
        assertThat(isRunning("$HTTPS_CONTROL_URL/status")).isTrue()

        val result = awaitResult("/status")
        assertThat(result.matches()).isFalse()
        assertThat(result.failureReasons()).isNotEmpty()
    }

    @Test
    fun `requests should be different when candidate doesn't exist`() {
        assertThat(isRunning("$HTTPS_CONTROL_URL/control")).isTrue()

        val result = awaitResult("/control")
        assertThat(result.matches()).isFalse()
        assertThat(result.failureReasons()).containsExactlyInAnyOrder(
                "Control returned status 200 and Candidate returned status 404.",
                "Content-Type is different: text/plain; charset=iso-8859-1 != null."
        )
    }

    @Test
    fun `request is mapped to another uri`() {
        assertThat(isRunning("$HTTPS_CONTROL_URL/mappedControl")).isTrue()

        val result = awaitResult("/mappedControl")
        assertThat(result.failureReasons()).isEmpty()
    }

    @Test
    fun `requests should be different when control doesn't exist`() {
        assertThat(isRunning("$HTTPS_CONTROL_URL/candidate")).isFalse()

        val result = awaitResult("/candidate")
        assertThat(result.matches()).isFalse()
        assertThat(result.failureReasons()).containsExactlyInAnyOrder(
                "Control returned status 404 and Candidate returned status 200.",
                "Content-Type is different: null != text/plain; charset=iso-8859-1."
        )
    }

    @Test
    fun `requests with json response is different`() {
        assertThat(isRunning("$HTTPS_CONTROL_URL/jsonDifferent")).isTrue()

        val result = awaitResult("/jsonDifferent")
        assertThat(result.matches()).isFalse()
        assertThat(result.failureReasons()).containsExactlyInAnyOrder(
                """{"op":"move","from":"/1","path":"/5"}"""
        )
    }

    @Test
    fun `requests with json response is same`() {
        assertThat(isRunning("$HTTPS_CONTROL_URL/json")).isTrue()

        val result = awaitResult("/json")
        assertThat(result.matches()).isTrue()
    }

}
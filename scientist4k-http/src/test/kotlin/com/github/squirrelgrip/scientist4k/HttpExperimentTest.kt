package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.cheti.Cheti
import com.github.squirrelgrip.cheti.getHostName
import com.github.squirrelgrip.cheti.getLocalAddress
import com.github.squirrelgrip.extensions.json.toInstance
import com.github.squirrelgrip.scientist4k.configuration.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.configuration.SslConfiguration
import com.github.squirrelgrip.scientist4k.model.Publisher
import com.github.squirrelgrip.scientist4k.model.Result
import org.apache.http.HttpResponse
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.TimeUnit


class HttpExperimentTest {

    companion object {
        private val HTTP_CONTROL_URL = "http://${getLocalAddress()}:9001"
        private val HTTPS_CONTROL_URL = "https://${getLocalAddress()}:9002"
        private val HTTP_CANDIDATE_URL = "http://${getLocalAddress()}:9011"
        private val HTTPS_CANDIDATE_URL = "https://${getLocalAddress()}:9012"
        private val HTTP_EXPERIMENT_URL = "http://${getLocalAddress()}:8999"
        private val HTTPS_EXPERIMENT_URL = "https://${getLocalAddress()}:9000"

        val httpExperimentConfiguration = File("experiment-config.json").toInstance<HttpExperimentConfiguration>()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val chetiConfigurationTemplate = Thread.currentThread().contextClassLoader.getResourceAsStream("cheti.json")
            val context = mapOf(
                    "IP_ADDRESS" to getLocalAddress(),
                    "HOSTNAME" to getHostName()
            )
            val cheti = Cheti()
            cheti.execute(cheti.loadConfiguration(chetiConfigurationTemplate, context))

            val controlServer = SecuredServer(CandidateHandler.serverConfiguration, CandidateHandler())
            val candidateServer = SecuredServer(ControlHandler.serverConfiguration, ControlHandler())

            controlServer.start()
            candidateServer.start()
        }

        fun isRunning(url: String): Boolean {
            val sslConfiguration = httpExperimentConfiguration.candidate.sslConfiguration
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

    var actualResult: Result<HttpResponse>? = null

    val publisher = object : Publisher<HttpResponse> {
        override fun publish(result: Result<HttpResponse>) {
            actualResult = result
        }
    }

    lateinit var experimentServer: SecuredServer

    @BeforeEach
    fun beforeEach() {
        actualResult = null
    }

    private fun createExperimentServer(controlUrl: String, candidateUrl: String) {
        assertIsRunning(controlUrl)
        assertIsRunning(candidateUrl)
        val control = httpExperimentConfiguration.control.copy(url = controlUrl)
        val candidate = httpExperimentConfiguration.candidate.copy(url  = candidateUrl)
        val configuration = httpExperimentConfiguration.copy(
                control = control,
                candidate = candidate
        )
        experimentServer = HttpExperimentServer(configuration)
        (experimentServer.handler as ExperimentHandler).experiment.addPublisher(publisher)
        experimentServer.start()
        assertIsRunning(HTTP_EXPERIMENT_URL)
    }

    @AfterEach
    fun afterEach() {
        experimentServer.stop()
    }

    private fun awaitResult(): Result<HttpResponse> {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until { actualResult != null }
        assertThat(actualResult).isNotNull()
        return actualResult!!
    }

    private fun assertIsRunning(url: String) {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until { isRunning("${url}/ok") }
    }

    @Test
    fun `requests should be the same`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/ok")).isTrue()

        assertThat(awaitResult().match).isTrue()
    }

    @Test
    fun `requests should be different when candidate doesn't exist`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/control")).isFalse()

        assertThat(awaitResult().match).isFalse()
    }

    @Test
    fun `requests should be different when control doesn't exist`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/candidate")).isTrue()

        assertThat(awaitResult().match).isFalse()
    }

}
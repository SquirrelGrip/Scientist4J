package com.github.squirrelgrip.scientist4k.http.server

import com.github.squirrelgrip.cheti.Cheti
import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.model.ExperimentFlag
import com.github.squirrelgrip.scientist4k.core.model.ExperimentFlag.RETURN_CANDIDATE
import com.github.squirrelgrip.scientist4k.http.core.consumer.FileConsumer
import com.github.squirrelgrip.scientist4k.http.core.extension.failureReasons
import com.github.squirrelgrip.scientist4k.http.core.extension.matches
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import com.github.squirrelgrip.scientist4k.http.test.handler.CandidateHandler
import com.github.squirrelgrip.scientist4k.http.test.handler.ControlHandler
import com.google.common.eventbus.Subscribe
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class SimpleHttpExperimentServerTest {

    companion object {
        private val HTTP_CONTROL_URL = "http://localhost:9001"
        private val HTTPS_CONTROL_URL = "https://localhost:9002"
        private val HTTP_CANDIDATE_URL = "http://localhost:9011"
        private val HTTPS_CANDIDATE_URL = "https://localhost:9012"
        private val HTTP_EXPERIMENT_URL = "http://localhost:8999"
        private val HTTPS_EXPERIMENT_URL = "https://localhost:9000"

        val httpExperimentConfiguration = File("experiment-config.json").toInstance<HttpExperimentConfiguration>()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            Cheti(File("../certs/cheti.json")).execute()

            val candidateServer = SecuredServer(CandidateHandler.serverConfiguration, CandidateHandler())
            val controlServer = SecuredServer(ControlHandler.serverConfiguration, ControlHandler())

            controlServer.start()
            candidateServer.start()
        }

        fun isRunning(url: String, expectedStatus: Int = 200): Boolean {
            val sslConfiguration = httpExperimentConfiguration.candidate.sslConfiguration
            val httpClient = HttpClients.custom().setSSLSocketFactory(
                SSLConnectionSocketFactory(
                    sslConfiguration!!.sslContext(),
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier()
                )
            ).build()
            val request = RequestBuilder.get(url).build()
            val response = httpClient.execute(request)
            return response.statusLine.statusCode == expectedStatus
        }
    }

    var actualResult: MutableList<HttpExperimentResult> = mutableListOf()

    @Subscribe
    fun receiveResult(experimentResult: HttpExperimentResult) {
        actualResult.add(experimentResult)
    }

    lateinit var testSubject: HttpExperimentServer

    @BeforeEach
    fun beforeEach() {
        AbstractExperiment.DEFAULT_EVENT_BUS.register(this)
        actualResult.clear()
    }

    @AfterEach
    fun afterEach() {
        AbstractExperiment.DEFAULT_EVENT_BUS.unregister(this)
        actualResult.clear()
        testSubject.stop()
    }

    private fun createExperimentServer(
        controlUrl: String,
        candidateUrl: String,
        experimentFlags: EnumSet<ExperimentFlag> = ExperimentFlag.DEFAULT
    ) {
        assertIsRunning(controlUrl)
        assertIsRunning(candidateUrl)
        val control = httpExperimentConfiguration.control.copy(url = controlUrl)
        val candidate = httpExperimentConfiguration.candidate.copy(url = candidateUrl)
        val experiment = httpExperimentConfiguration.experiment.copy(experimentFlags = experimentFlags)
        val configuration = httpExperimentConfiguration.copy(
            experiment = experiment,
            control = control,
            candidate = candidate,
        )
        testSubject = HttpExperimentServer(configuration)
        testSubject.start()
        val file = File(File(System.getenv("user.dir"), ".."), "scientist4k-api/report")
        (testSubject.handler as HttpExperimentHandler).httpExperiment.eventBus.register(FileConsumer(file))

        assertIsRunning(HTTP_EXPERIMENT_URL)
    }

    private fun getResult(uri: String): HttpExperimentResult? {
        return actualResult.firstOrNull {
            it.request.url == uri
        }
    }

    private fun awaitResult(url: String): HttpExperimentResult {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until {
            getResult(url) != null
        }
        val result = getResult(url)
        assertNotNull(result)
        return result!!
    }

    private fun assertIsRunning(url: String) {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until { isRunning("${url}/ok") }
    }

    @Test
    fun `requests should be the same`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("$HTTPS_EXPERIMENT_URL/ok")).isTrue()

        val result = awaitResult("/ok")
        assertThat(result.matches()).isTrue()
        assertThat(result.failureReasons()).isEmpty()
    }

    @Test
    fun `requests with different status`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("$HTTPS_EXPERIMENT_URL/status")).isTrue()

        val result = awaitResult("/status")
        assertThat(result.matches()).isFalse()
        assertThat(result.failureReasons()).containsExactlyInAnyOrder(
            "Control returned status 200 and Candidate returned status 201."
        )
    }

    @Test
    fun `requests with different status and candidate is returned`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL, EnumSet.of(RETURN_CANDIDATE))

        assertThat(isRunning("$HTTPS_EXPERIMENT_URL/status", 201)).isTrue()

        val result = awaitResult("/status")
        assertThat(result.matches()).isFalse()
        assertThat(result.failureReasons()).containsExactlyInAnyOrder(
            "Control returned status 200 and Candidate returned status 201."
        )
    }

    @Test
    fun `requests should be different when candidate doesn't exist`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("$HTTPS_EXPERIMENT_URL/control")).isTrue()

        val result = awaitResult("/control")
        assertThat(result.matches()).isFalse()
        assertThat(result.failureReasons()).containsExactlyInAnyOrder(
            "Control returned status 200 and Candidate returned status 404.",
            "Content-Type is different: text/plain; charset=iso-8859-1 != null."
        )
    }

    @Test
    fun `requests should be different when candidate doesn't exist and candidate is returned`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL, EnumSet.of(RETURN_CANDIDATE))

        assertThat(isRunning("$HTTPS_EXPERIMENT_URL/control", 404)).isTrue()

        val result = awaitResult("/control")
        assertThat(result.matches()).isFalse()
        assertThat(result.failureReasons()).containsExactlyInAnyOrder(
            "Control returned status 200 and Candidate returned status 404.",
            "Content-Type is different: text/plain; charset=iso-8859-1 != null."
        )
    }

    @Test
    fun `request is mapped to another uri`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("$HTTPS_EXPERIMENT_URL/mappedControl")).isTrue()

        val result = awaitResult("/mappedControl")
        assertThat(result.matches()).isTrue()
        assertThat(result.failureReasons()).isEmpty()
    }

    @Test
    fun `requests should be different when control doesn't exist`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("$HTTPS_EXPERIMENT_URL/candidate")).isFalse()

        val result = awaitResult("/candidate")
        assertThat(result.matches()).isFalse()
        assertThat(result.failureReasons()).containsExactlyInAnyOrder(
            "Control returned status 404 and Candidate returned status 200.",
            "Content-Type is different: null != text/plain; charset=iso-8859-1."
        )
    }

    @Test
    fun `requests with json response is different`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("$HTTPS_EXPERIMENT_URL/jsonDifferent")).isTrue()

        val result = awaitResult("/jsonDifferent")
        assertThat(result.matches()).isFalse()
        assertThat(result.failureReasons()).containsExactlyInAnyOrder(
            """{"op":"move","from":"/1","path":"/5"}"""
        )
    }

    @Test
    fun `requests with json response is same`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("$HTTPS_EXPERIMENT_URL/json")).isTrue()

        val result = awaitResult("/json")
        assertThat(result.matches()).isTrue()
    }

}

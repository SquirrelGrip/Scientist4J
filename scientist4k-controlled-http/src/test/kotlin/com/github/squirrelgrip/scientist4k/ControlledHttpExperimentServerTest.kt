package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.cheti.Cheti
import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.configuration.ControlledHttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.handler.CandidateHandler
import com.github.squirrelgrip.scientist4k.handler.ControlHandler
import com.github.squirrelgrip.scientist4k.handler.ReferenceHandler
import com.github.squirrelgrip.scientist4k.model.ControlledResult
import com.github.squirrelgrip.scientist4k.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.server.SecuredServer
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
import java.util.concurrent.TimeUnit

class ControlledHttpExperimentServerTest {

    companion object {
        private const val HTTP_CONTROL_URL = "http://localhost:9001"
        private const val HTTPS_CONTROL_URL = "https://localhost:9002"
        private const val HTTP_REFERENCE_URL = "http://localhost:9003"
        private const val HTTPS_REFERENCE_URL = "https://localhost:9004"
        private const val HTTP_CANDIDATE_URL = "http://localhost:9011"
        private const val HTTPS_CANDIDATE_URL = "https://localhost:9012"
        private const val HTTP_EXPERIMENT_URL = "http://localhost:8999"
        private const val HTTPS_EXPERIMENT_URL = "https://localhost:9000"

        val controlledHttpExperimentConfiguration = File("controlled-experiment-config.json").toInstance<ControlledHttpExperimentConfiguration>()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val chetiConfiguration = Thread.currentThread().contextClassLoader.getResourceAsStream("cheti.json")
            val cheti = Cheti(chetiConfiguration)
            cheti.execute()

            val primaryControlServer = SecuredServer(ControlHandler.serverConfiguration, ControlHandler())
            val secondaryControlServer = SecuredServer(ReferenceHandler.serverConfiguration, ReferenceHandler())
            val candidateServer = SecuredServer(CandidateHandler.serverConfiguration, CandidateHandler())

            primaryControlServer.start()
            secondaryControlServer.start()
            candidateServer.start()
        }

        fun isRunning(url: String): Boolean {
            val sslConfiguration = controlledHttpExperimentConfiguration.candidate.sslConfiguration
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

    var actualResult: MutableList<ControlledResult<ExperimentResponse>> = mutableListOf()

    @Subscribe
    fun receiveResult(result: ControlledResult<ExperimentResponse>) {
        println(result.sample.notes["request"])
        actualResult.add(result)
    }

    lateinit var testSubject: ControlledHttpExperimentServer

    @BeforeEach
    fun beforeEach() {
        actualResult.clear()
    }

    private fun createExperimentServer(controlUrl: String, referenceUrl: String, candidateUrl: String) {
        assertIsRunning(controlUrl)
        assertIsRunning(referenceUrl)
        assertIsRunning(candidateUrl)
        val control = controlledHttpExperimentConfiguration.control.copy(url = controlUrl)
        val reference = controlledHttpExperimentConfiguration.reference.copy(url = referenceUrl)
        val candidate = controlledHttpExperimentConfiguration.candidate.copy(url = candidateUrl)
        val configuration = controlledHttpExperimentConfiguration.copy(
                control = control,
                reference = reference,
                candidate = candidate
        )
        testSubject = ControlledHttpExperimentServer(configuration)
        (testSubject.handler as ControlledExperimentHandler).controlledHttpExperiment.eventBus.register(this)
        testSubject.start()
        assertIsRunning(HTTP_EXPERIMENT_URL)
    }

    @AfterEach
    fun afterEach() {
        testSubject.stop()
    }

    private fun getResult(uri: String): ControlledResult<ExperimentResponse>? {
        return actualResult.firstOrNull {
            it.sample.notes["uri"] == uri
        }
    }

    private fun awaitResult(url: String): ControlledResult<ExperimentResponse> {
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
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_REFERENCE_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/ok")).isTrue()

        val result = awaitResult("/ok")
        assertThat(result.match.matches).isTrue()
        assertThat(result.match.failureReasons).isEmpty()
    }

    @Test
    fun `requests with different status`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_REFERENCE_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/status")).isTrue()

        val result = awaitResult("/status")
        assertThat(result.match.matches).isFalse()
        assertThat(result.match.failureReasons).containsExactlyInAnyOrder(
                "Control returned status 200 and Candidate returned status 201."
        )
    }

    @Test
    fun `requests should be different when candidate doesn't exist`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_REFERENCE_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/control")).isTrue()

        val result = awaitResult("/control")
        assertThat(result.match.matches).isFalse()
        assertThat(result.match.failureReasons).containsExactlyInAnyOrder(
                "Control returned status 200 and Candidate returned status 404.",
                "Content-Type is different: text/plain != null."
        )
    }

    @Test
    fun `request is mapped to another uri`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_REFERENCE_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/mappedControl")).isTrue()

        val result = awaitResult("/mappedControl")
        assertThat(result.match.matches).isTrue()
        assertThat(result.match.failureReasons).isEmpty()
    }

    @Test
    fun `requests should be different when control doesn't exist`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_REFERENCE_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/candidate")).isFalse()

        val result = awaitResult("/candidate")
        assertThat(result.match.matches).isFalse()
        assertThat(result.match.failureReasons).containsExactlyInAnyOrder(
                "Control returned status 404 and Candidate returned status 200.",
                "Content-Type is different: null != text/plain."
        )
    }

    @Test
    fun `requests with json response is different`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_REFERENCE_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/jsonDifferent")).isTrue()

        val result = awaitResult("/jsonDifferent")
        assertThat(result.match.matches).isFalse()
        assertThat(result.match.failureReasons).containsExactlyInAnyOrder(
                "1 in control, not in candidate",
                "5 in candidate, not in control"
        )
    }

    @Test
    fun `requests with json response is same`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_REFERENCE_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/json")).isTrue()

        val result = awaitResult("/json")
        assertThat(result.match.matches).isTrue()
    }

}
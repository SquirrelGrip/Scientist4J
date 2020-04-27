package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.cheti.Cheti
import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.configuration.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.handler.CandidateHandler
import com.github.squirrelgrip.scientist4k.handler.ControlHandler
import com.github.squirrelgrip.scientist4k.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.model.Publisher
import com.github.squirrelgrip.scientist4k.model.Result
import com.github.squirrelgrip.scientist4k.server.SecuredServer
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


class HttpExperimentServerTest {

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
            val chetiConfiguration = Thread.currentThread().contextClassLoader.getResourceAsStream("cheti.json")
            val cheti = Cheti(chetiConfiguration)
            cheti.execute()

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

    var actualResult: MutableList<Result<ExperimentResponse>> = mutableListOf()

    val publisher = object : Publisher<ExperimentResponse> {
        override fun publish(result: Result<ExperimentResponse>) {
            println(result.sample.notes["request"])
            actualResult.add(result)
        }
    }

    lateinit var testSubject: HttpExperimentServer

    @BeforeEach
    fun beforeEach() {
        actualResult.clear()
    }

    private fun createExperimentServer(controlUrl: String, candidateUrl: String) {
        assertIsRunning(controlUrl)
        assertIsRunning(candidateUrl)
        val control = httpExperimentConfiguration.control.copy(url = controlUrl)
        val candidate = httpExperimentConfiguration.candidate.copy(url = candidateUrl)
        val configuration = httpExperimentConfiguration.copy(
                control = control,
                candidate = candidate
        )
        testSubject = HttpExperimentServer(configuration)
        (testSubject.handler as ExperimentHandler).experiment.addPublisher(publisher)
        testSubject.start()
        assertIsRunning(HTTP_EXPERIMENT_URL)
    }

    @AfterEach
    fun afterEach() {
        testSubject.stop()
    }

    private fun getResult(uri: String): Result<ExperimentResponse>? {
        return actualResult.firstOrNull {
            it.sample.notes["uri"] == uri
        }
    }

    private fun awaitResult(url: String): Result<ExperimentResponse> {
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

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/ok")).isTrue()

        val result = awaitResult("/ok")
        assertThat(result.match.matches).isTrue()
        assertThat(result.match.failureReasons).isEmpty()
    }

    @Test
    fun `requests with different status`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/status")).isTrue()

        val result = awaitResult("/status")
        assertThat(result.match.matches).isFalse()
        assertThat(result.match.failureReasons).containsExactlyInAnyOrder(
                "Control returned status 200 and Candidate returned status 201."
        )
    }

    @Test
    fun `requests should be different when candidate doesn't exist`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

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
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/mappedControl")).isTrue()

        val result = awaitResult("/mappedControl")
        assertThat(result.match.matches).isTrue()
        assertThat(result.match.failureReasons).isEmpty()
    }

    @Test
    fun `requests should be different when control doesn't exist`() {
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

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
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

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
        createExperimentServer(HTTPS_CONTROL_URL, HTTPS_CANDIDATE_URL)

        assertThat(isRunning("${HTTPS_EXPERIMENT_URL}/json")).isTrue()

        val result = awaitResult("/json")
        assertThat(result.match.matches).isTrue()
    }

}
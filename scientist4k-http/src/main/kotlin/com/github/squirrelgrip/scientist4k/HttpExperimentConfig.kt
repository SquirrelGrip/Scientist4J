package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
import org.apache.http.HttpResponse

data class HttpExperimentConfig(
        val name: String,
        val context: Map<String, Any> = emptyMap(),
        val raiseOnMismatch: Boolean = false,
        val metricsProvider: String,
        val comparator: ExperimentComparator<HttpResponse> = HttpResponseComparator(),
        val sampleFactory: SampleFactory = SampleFactory(),
        val controlUrl: String,
        val candidateUrl: String,
        val allowedMethods: List<String> = listOf("GET"),
        val controlSslConfiguration: SslConfiguration? = null,
        val candidateSslConfiguration: SslConfiguration? = null
)

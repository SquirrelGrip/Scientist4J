package com.github.squirrelgrip.scientist4k.comparator

import com.github.squirrelgrip.scientist4k.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.model.ComparisonResult.Companion.SUCCESS
import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.model.ExperimentResponse
import org.apache.http.ProtocolVersion
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExperimentResponseComparator(
        val debug: Boolean = false
) : ExperimentComparator<ExperimentResponse?> {
    private val statusLineComparator = StatusLineComparator()
    private val headersComparator = HeadersComparator()
    private val contentComparator = ContentComparator()

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ExperimentComparator::class.java)
    }
    override fun invoke(control: ExperimentResponse?, candidate: ExperimentResponse?): ComparisonResult {
        if (control != null && candidate != null) {
            LOGGER.info("Comparing StatusLine...")
            val statusLineMatch = statusLineComparator.invoke(control, candidate)
            LOGGER.info("Comparing Headers...")
            val headerMatch = headersComparator.invoke(control, candidate)
            LOGGER.info("Comparing Contents...")
            val contentMatch = contentComparator.invoke(control, candidate)
            return ComparisonResult(statusLineMatch, headerMatch, contentMatch)
        }
        return ComparisonResult("Either Control or Candidate responses is null.")
    }
}

class StatusLineComparator : ExperimentComparator<ExperimentResponse> {
    private val statusComparator = StatusComparator()
    private val protocolComparator = ProtocolComparator()
    override fun invoke(control: ExperimentResponse, candidate: ExperimentResponse): ComparisonResult {
        val controlStatus = control.status
        val candidateStatus = candidate.status
        val statusCodeMatch = statusComparator.invoke(controlStatus.statusCode, candidateStatus.statusCode)
        val protocolMatch = protocolComparator.invoke(controlStatus.protocolVersion, candidateStatus.protocolVersion)
        return ComparisonResult(statusCodeMatch, protocolMatch)
    }
}

class StatusComparator : ExperimentComparator<Int> {
    override fun invoke(control: Int, candidate: Int): ComparisonResult {
        if (control == candidate) {
            return SUCCESS
        }
        return ComparisonResult("Control returned status $control and Candidate returned status $candidate.")
    }
}

class ProtocolComparator : ExperimentComparator<ProtocolVersion> {
    override fun invoke(control: ProtocolVersion, candidate: ProtocolVersion): ComparisonResult {
        if (control == candidate) {
            return SUCCESS
        }
        return ComparisonResult("Control protocol $control and Candidate protocol $candidate did not match.")
    }
}


fun List<String>.toComparisonResult(): ComparisonResult = ComparisonResult(this)


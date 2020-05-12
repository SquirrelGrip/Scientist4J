package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.google.common.net.HttpHeaders.CONTENT_TYPE
import com.google.common.net.MediaType
import com.google.common.net.MediaType.JSON_UTF_8
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ContentComparator : ExperimentComparator<ExperimentResponse> {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(StatusComparator::class.java)
        private val defaultContentTypeComparator: ContentTypeComparator = DefaultContentTypeComparator()
        private val contentComparators: Map<MediaType, ContentTypeComparator> = mapOf(
                JSON_UTF_8 to JsonContentTypeComparator()
        )
    }

    override fun invoke(control: ExperimentResponse, candidate: ExperimentResponse): ComparisonResult {
        LOGGER.trace("Comparing Contents...")
        val controlContentType = control.mediaType
        val candidateContentType = candidate.mediaType
        if (controlContentType != null && controlContentType.withoutParameters() == candidateContentType?.withoutParameters()) {
            val controlContent = control.body
            val candidateContent = candidate.body
            return getContentComparator(controlContentType).invoke(controlContent, candidateContent)
        }
        return ComparisonResult("$CONTENT_TYPE is different: $controlContentType != $candidateContentType.")
    }

    private fun getContentComparator(mediaType: MediaType): ContentTypeComparator =
            contentComparators
                    .filterKeys {
                        mediaType.withoutParameters().`is`(it.withoutParameters())
                    }
                    .values
                    .firstOrNull() ?: defaultContentTypeComparator

}



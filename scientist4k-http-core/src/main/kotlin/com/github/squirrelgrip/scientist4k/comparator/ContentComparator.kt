package com.github.squirrelgrip.scientist4k.comparator

import com.github.squirrelgrip.scientist4k.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.model.ExperimentResponse
import com.google.common.net.HttpHeaders.CONTENT_TYPE
import com.google.common.net.MediaType
import com.google.common.net.MediaType.JSON_UTF_8

class ContentComparator : ExperimentComparator<ExperimentResponse> {
    private val defaultContentTypeComparator: ContentTypeComparator = DefaultContentTypeComparator()
    private val contentComparators: Map<MediaType, ContentTypeComparator> = mapOf(
            JSON_UTF_8.withoutParameters() to JsonContentTypeComparator()
    )

    override fun invoke(control: ExperimentResponse, candidate: ExperimentResponse): ComparisonResult {
        val controlContentType = control.toMediaType()
        val candidateContentType = candidate.toMediaType()
        if (controlContentType != null && controlContentType == candidateContentType) {
            val controlContent = control.content
            val candidateContent = candidate.content
            return getContentComparator(controlContentType).invoke(controlContent, candidateContent)
        }
        return ComparisonResult("$CONTENT_TYPE is different: $controlContentType != $candidateContentType.")
    }

    private fun getContentComparator(contentType: MediaType): ContentTypeComparator =
            contentComparators[contentType] ?: defaultContentTypeComparator
}

private fun ExperimentResponse.toMediaType(): MediaType? {
    val contentType = this.getContentType()
    return if (contentType != null) {
        MediaType.parse(contentType)?.withoutParameters()
    } else {
        null
    }
}

private fun ExperimentResponse.getContentType(): String? {
    return this.headers.firstOrNull { it.name == CONTENT_TYPE }?.value
}


package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.fasterxml.jackson.databind.JsonNode
import com.flipkart.zjsonpatch.JsonDiff
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult.Companion.SUCCESS

class JsonContentTypeComparator : ContentTypeComparator {

    override fun invoke(control: String?, candidate: String?): ComparisonResult {
        val controlJsonNode = control?.toJsonNode()
        val candidateJsonNode = candidate?.toJsonNode()

        val diff: JsonNode = JsonDiff.asJson(controlJsonNode, candidateJsonNode)
        val list = diff.asIterable().toList()
        return if (list.isEmpty()) {
            SUCCESS
        } else {
            ComparisonResult(
                *list.map {
                    it.toJson()
                }.toTypedArray()
            )
        }
    }

}

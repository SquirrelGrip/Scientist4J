package com.github.squirrelgrip.scientist4k.comparator

import com.github.squirrelgrip.extensions.json.toInstance
import com.github.squirrelgrip.extensions.map.flatten
import com.github.squirrelgrip.scientist4k.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.model.ComparisonResult.Companion.SUCCESS
import com.google.common.collect.MapDifference
import com.google.common.collect.Maps

class JsonContentTypeComparator : ContentTypeComparator {

    override fun invoke(control: ByteArray, candidate: ByteArray): ComparisonResult {
        val controlMap: Map<String, Any?> = control.toInstance()
        val candidateMap: Map<String, Any> = candidate.toInstance()

        val difference: MapDifference<String, Any> = Maps.difference(controlMap.flatten(), candidateMap.flatten())

        return if (difference.areEqual()) {
            SUCCESS
        } else {
            ComparisonResult(
                    *difference.entriesOnlyOnLeft()
                            .map { (key, _) -> "$key in control, not in candidate" }.toTypedArray(),
                    *difference.entriesOnlyOnRight()
                            .map { (key, _) -> "$key in candidate, not in control" }.toTypedArray(),
                    *difference.entriesDiffering()
                            .map { (key, value) -> "$key has different values (${value.leftValue()} != ${value.rightValue()})" }.toTypedArray()
            )
        }
    }

}

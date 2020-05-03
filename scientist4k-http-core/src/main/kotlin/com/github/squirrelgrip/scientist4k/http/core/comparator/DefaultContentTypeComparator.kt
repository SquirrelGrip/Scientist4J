package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult

class DefaultContentTypeComparator: ContentTypeComparator {
    override fun invoke(control: ByteArray, candidate: ByteArray): ComparisonResult {
        return if (control.contentEquals(candidate)) {
            ComparisonResult.SUCCESS
        } else {
            ComparisonResult("Contents are different.")
        }
    }

}

package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult

class DefaultContentTypeComparator: ContentTypeComparator {
    override fun invoke(control: String?, candidate: String?): ComparisonResult {
        return if (control?.trim() == candidate?.trim()) {
            ComparisonResult.SUCCESS
        } else {
            ComparisonResult("Contents are different.")
        }
    }

}

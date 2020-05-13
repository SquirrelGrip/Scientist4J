package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.github.squirrelgrip.extension.json.toJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JsonContentTypeComparatorTest {
    @Test
    fun `json is the same`() {
        val listA = listOf(
                Example(1, "SSS", mapOf(1 to "AAA"), listOf(Item(1, "AAA1"), Item(2, "BBB1"))),
                Example(2, "RRR", mapOf(1 to "BBB"), listOf(Item(1, "AAA2"), Item(2, "BBB2"))),
                Example(3, "QQQ", mapOf(1 to "CCC"), listOf(Item(1, "AAA3"), Item(2, "BBB3"))),
                Example(4, "PPP", mapOf(1 to "DDD"), listOf(Item(1, "AAA4"), Item(2, "BBB4")))
        )
        val listB = listOf(
                Example(1, "SSS", mapOf(1 to "AAA"), listOf(Item(1, "AAA1"), Item(2, "BBB1"))),
                Example(2, "RRR", mapOf(1 to "BBB"), listOf(Item(1, "AAA2"), Item(2, "BBB2"))),
                Example(3, "QQQ", mapOf(1 to "CCC"), listOf(Item(1, "AAA3"), Item(2, "BBB3"))),
                Example(4, "PPP", mapOf(1 to "DDD"), listOf(Item(1, "AAA4"), Item(2, "BBB4")))
        )
        val comparisonResult = JsonContentTypeComparator().invoke(listA.toJson(), listB.toJson())
        assertThat(comparisonResult.matches).isTrue()
    }

    @Test
    fun `json when outer list has swapped`() {
        val listA = listOf(
                Example(1, "SSS", mapOf(1 to "AAA"), listOf(Item(1, "AAA1"), Item(2, "BBB1"))),
                Example(2, "RRR", mapOf(1 to "BBB"), listOf(Item(1, "AAA2"), Item(2, "BBB2"))),
                Example(3, "QQQ", mapOf(1 to "CCC"), listOf(Item(1, "AAA3"), Item(2, "BBB3"))),
                Example(4, "PPP", mapOf(1 to "DDD"), listOf(Item(1, "AAA4"), Item(2, "BBB4")))
        )
        val listB = listOf(
                Example(1, "SSS", mapOf(1 to "AAA"), listOf(Item(1, "AAA1"), Item(2, "BBB1"))),
                Example(3, "QQQ", mapOf(1 to "CCC"), listOf(Item(1, "AAA3"), Item(2, "BBB3"))),
                Example(2, "RRR", mapOf(1 to "BBB"), listOf(Item(1, "AAA2"), Item(2, "BBB2"))),
                Example(4, "PPP", mapOf(1 to "DDD"), listOf(Item(1, "AAA4"), Item(2, "BBB4")))
        )
        val comparisonResult = JsonContentTypeComparator().invoke(listA.toJson(), listB.toJson())
        assertThat(comparisonResult.matches).isFalse()
    }

    @Test
    fun `json when inner list has swapped`() {
        val listA = listOf(
                Example(1, "SSS", mapOf(1 to "AAA"), listOf(Item(1, "AAA1"), Item(2, "BBB1"))),
                Example(2, "RRR", mapOf(1 to "BBB"), listOf(Item(1, "AAA2"), Item(2, "BBB2"))),
                Example(3, "QQQ", mapOf(1 to "CCC"), listOf(Item(1, "AAA3"), Item(2, "BBB3"))),
                Example(4, "PPP", mapOf(1 to "DDD"), listOf(Item(1, "AAA4"), Item(2, "BBB4")))
        )
        val listB = listOf(
                Example(1, "SSS", mapOf(1 to "AAA"), listOf(Item(1, "AAA1"), Item(2, "BBB1"))),
                Example(2, "RRR", mapOf(1 to "BBB"), listOf(Item(2, "BBB2"), Item(1, "AAA2"))),
                Example(3, "QQQ", mapOf(1 to "CCC"), listOf(Item(1, "AAA3"), Item(2, "BBB3"))),
                Example(4, "PPP", mapOf(1 to "DDD"), listOf(Item(1, "AAA4"), Item(2, "BBB4")))
        )
        val comparisonResult = JsonContentTypeComparator().invoke(listA.toJson(), listB.toJson())
        assertThat(comparisonResult.matches).isFalse()
    }

    @Test
    fun `json when both inner list and out list has swapped`() {
        val listA = listOf(
                Example(1, "SSS", mapOf(1 to "AAA"), listOf(Item(1, "AAA1"), Item(2, "BBB1"))),
                Example(2, "RRR", mapOf(1 to "BBB"), listOf(Item(1, "AAA2"), Item(2, "BBB2"))),
                Example(3, "QQQ", mapOf(1 to "CCC"), listOf(Item(1, "AAA3"), Item(2, "BBB3"))),
                Example(4, "PPP", mapOf(1 to "DDD"), listOf(Item(1, "AAA4"), Item(2, "BBB4")))
        )
        val listB = listOf(
                Example(1, "SSS", mapOf(1 to "AAA"), listOf(Item(1, "AAA1"), Item(2, "BBB1"))),
                Example(3, "QQQ", mapOf(1 to "CCC"), listOf(Item(1, "AAA3"), Item(2, "BBB3"))),
                Example(2, "RRR", mapOf(1 to "BBB"), listOf(Item(2, "BBB2"), Item(1, "AAA2"))),
                Example(4, "PPP", mapOf(1 to "DDD"), listOf(Item(1, "AAA4"), Item(2, "BBB4")))
        )
        val comparisonResult = JsonContentTypeComparator().invoke(listA.toJson(), listB.toJson())
        assertThat(comparisonResult.matches).isFalse()
    }

    @Test
    fun `json when both inner list and outer list has swapped and 1 swapped with 4`() {
        val listA = listOf(
                Example(1, "SSS", mapOf(1 to "AAA"), listOf(Item(1, "AAA1"), Item(2, "BBB1"))),
                Example(2, "RRR", mapOf(1 to "BBB"), listOf(Item(1, "AAA2"), Item(2, "BBB2"))),
                Example(3, "QQQ", mapOf(1 to "CCC"), listOf(Item(1, "AAA3"), Item(2, "BBB3"))),
                Example(4, "PPP", mapOf(1 to "DDD"), listOf(Item(1, "AAA4"), Item(2, "BBB4")))
        )
        val listB = listOf(
                Example(4, "PPP", mapOf(1 to "DDD"), listOf(Item(1, "AAA4"), Item(2, "BBB4"))),
                Example(3, "QQQ", mapOf(1 to "CCC"), listOf(Item(1, "AAA3"), Item(2, "BBB3"))),
                Example(2, "RRR", mapOf(1 to "BBB"), listOf(Item(2, "BBB2"), Item(1, "AAA2"))),
                Example(1, "SSS", mapOf(1 to "AAA"), listOf(Item(1, "AAA1"), Item(2, "BBB1")))
        )
        val comparisonResult = JsonContentTypeComparator().invoke(listA.toJson(), listB.toJson())
        assertThat(comparisonResult.matches).isFalse()
    }
}

data class Item(
        val i: Int,
        val s: String
)

data class Example(
        val i: Int,
        val s: String,
        val m: Map<Int, String>,
        val l: List<Item>
)

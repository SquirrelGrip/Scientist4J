package com.github.squirrelgrip.scientist4k.http.core.configuration

import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import java.util.*
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest

data class MappingsConfiguration(
    val mappings: List<MappingConfiguration> = emptyList()
) {
    fun getRunOptions(inboundRequest: ServletRequest): EnumSet<ExperimentOption> =
        getMappingConfiguration(inboundRequest)?.options ?: ExperimentOption.DEFAULT

    fun getMappingConfiguration(inboundRequest: ServletRequest): MappingConfiguration? =
        if (inboundRequest is HttpServletRequest) {
            mappings.firstOrNull {
                inboundRequest.pathInfo != null && it.matches(inboundRequest.pathInfo)
            }
        } else {
            null
        }

    fun replace(url: String): String {
        var newUrl = url
        mappings.filter {
            it.matches(url)
        }.forEach {
            newUrl = it.replace(newUrl)
        }
        return newUrl
    }

}

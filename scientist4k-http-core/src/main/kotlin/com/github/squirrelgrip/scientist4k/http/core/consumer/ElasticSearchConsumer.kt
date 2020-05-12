package com.github.squirrelgrip.scientist4k.http.core.consumer

import com.github.squirrelgrip.extension.json.toJson
import com.google.common.eventbus.Subscribe
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.ContentType.APPLICATION_JSON
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients

class ElasticSearchConsumer<T>(
        val url: String
) {
    @Subscribe
    fun receiveResult(experimentResult: Any) {
        val httpClient = HttpClients.createDefault()
        val json = experimentResult.toJson()
        val request = RequestBuilder.create("POST").apply {
            setUri(url)
            entity = StringEntity(json, APPLICATION_JSON)
        }.build()
        val response = httpClient.execute(request)
        val statusCode = response.statusLine.statusCode
        if (statusCode in 200..299) {
            println("Successfully published experimentResult")
        } else {
            println("Publishing result failed with status $statusCode")
        }
    }
}
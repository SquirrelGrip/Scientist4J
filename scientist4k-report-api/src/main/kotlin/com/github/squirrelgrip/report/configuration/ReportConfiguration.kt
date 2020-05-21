package com.github.squirrelgrip.report.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import java.io.File

@Configuration
@PropertySource(value = ["classpath:application.properties"])
class ReportConfiguration {
    @Value("\${baseDirectory}")
    lateinit var baseDirectory: String

    @Bean
    fun rootDirectory() : File {
        return File(baseDirectory)
    }
}
package com.github.squirrelgrip.report

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.github.squirrelgrip.report"])
class Application {
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
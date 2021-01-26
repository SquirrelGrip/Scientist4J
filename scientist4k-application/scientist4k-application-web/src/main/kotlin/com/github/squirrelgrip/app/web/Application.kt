package com.github.squirrelgrip.app.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.github.squirrelgrip.app.common", "com.github.squirrelgrip.app.web", "com.github.squirrelgrip.app.api"])
class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args)
        }
    }
}


package com.github.squirrelgrip.scientist4k.http.core.exception

class InvalidConfigurationException(message: String) : Exception(message) {
    constructor(): this("Invalid Configuration")
}

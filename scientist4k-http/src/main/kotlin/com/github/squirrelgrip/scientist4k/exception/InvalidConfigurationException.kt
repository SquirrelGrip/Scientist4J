package com.github.squirrelgrip.configuration.exception

class InvalidConfigurationException(message: String) : Exception(message) {
    constructor(): this("Invalid Configuration")

}

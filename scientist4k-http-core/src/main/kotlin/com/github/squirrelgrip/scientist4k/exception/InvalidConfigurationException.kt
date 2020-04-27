package com.github.squirrelgrip.scientist4k.exception

class InvalidConfigurationException(message: String) : Exception(message) {
    constructor(): this("Invalid Configuration")

}

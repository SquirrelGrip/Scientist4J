package com.github.squirrelgrip.scientist4k.extension

import com.fasterxml.jackson.databind.ObjectMapper

fun Any.toJson() = ObjectMapper().writeValueAsString(this)
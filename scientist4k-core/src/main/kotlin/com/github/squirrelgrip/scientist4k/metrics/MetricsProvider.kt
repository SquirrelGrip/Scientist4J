package com.github.squirrelgrip.scientist4k.metrics

interface MetricsProvider<T> {
    fun timer(vararg nameComponents: String): Timer
    fun counter(vararg nameComponents: String): Counter

    val registry: T

    companion object {
        fun build(type: String): MetricsProvider<*> {
            val javaClass = when (type) {
                "DROPWIZARD" -> "com.github.squirrelgrip.scientist4k.metrics.dropwizard.DropwizardMetricsProvider"
                "MICROMETER" -> "com.github.squirrelgrip.scientist4k.metrics.micrometer.MicrometerMetricsProvider"
                else -> "com.github.squirrelgrip.scientist4k.metrics.NoopMetricsProvider"
            }
            return Class.forName(javaClass).newInstance() as MetricsProvider<*>
        }
    }
}
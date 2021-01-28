package com.github.squirrelgrip.scientist4k.metrics

enum class Metrics(val clazzName: String) {
    DROPWIZARD("com.github.squirrelgrip.scientist4k.metrics.dropwizard.DropwizardMetricsProvider"),
    MICROMETER("com.github.squirrelgrip.scientist4k.metrics.micrometer.MicrometerMetricsProvider"),
    NOOP("com.github.squirrelgrip.scientist4k.metrics.noop.NoopMetricsProvider");

    fun getProvider(): MetricsProvider<*> {
        return Class.forName(clazzName).getDeclaredConstructor().newInstance() as MetricsProvider<*>
    }
}
package com.takaotech.ktravel

import com.codahale.metrics.Slf4jReporter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.dropwizard.DropwizardMetrics
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private const val REPORT_PERIOD_SECONDS = 10L

/**
 * Dropwizard metrics, reported to SLF4J every [REPORT_PERIOD_SECONDS] seconds.
 *
 * JVM only: `ktor-server-metrics` publishes no native variant, and Dropwizard itself is built on
 * `java.util.concurrent`.
 */
fun Application.configureMonitoring() {
    install(DropwizardMetrics) {
        Slf4jReporter.forRegistry(registry)
            .outputTo(LoggerFactory.getLogger("metrics"))
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start(REPORT_PERIOD_SECONDS, TimeUnit.SECONDS)
    }
}

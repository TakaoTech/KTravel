package com.takaotech.ktravel.core.logging.slf4j

import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.Logger
import org.slf4j.helpers.BasicMDCAdapter
import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider
import java.util.concurrent.ConcurrentHashMap

/** The SLF4J version this binding is written against; SLF4J only compares the major and minor. */
private const val REQUESTED_API_VERSION = "2.0.99"

/**
 * Registers Kermit as the SLF4J backend of the application.
 *
 * Found through `ServiceLoader`, by way of the `META-INF/services` entry in the JVM and Android
 * resources — which is why the shrinker is told to keep this class: nothing references it by name.
 *
 * Being the only provider on the classpath is the point. logback is deliberately absent from the
 * application (see `composeApp/build.gradle.kts`), so there is nothing to compete with, and the
 * standalone server keeps logback because this class never reaches it.
 */
class KermitSlf4jServiceProvider : SLF4JServiceProvider {

    private val loggers = ConcurrentHashMap<String, Logger>()

    private val loggerFactory = ILoggerFactory { name ->
        loggers.getOrPut(name) { KermitSlf4jLogger(name) }
    }

    private val markerFactory: IMarkerFactory = BasicMarkerFactory()

    private val mdcAdapter: MDCAdapter = BasicMDCAdapter()

    override fun getLoggerFactory(): ILoggerFactory = loggerFactory

    override fun getMarkerFactory(): IMarkerFactory = markerFactory

    override fun getMDCAdapter(): MDCAdapter = mdcAdapter

    override fun getRequestedApiVersion(): String = REQUESTED_API_VERSION

    override fun initialize() = Unit
}

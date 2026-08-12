plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
}

// Deployment entry point for :gunzou-navigator. It exists as a separate module because the Ktor
// Gradle plugin disables buildFatJar and runDocker on a multiplatform project (KTOR-8464), and the
// workaround JetBrains recommends is exactly this: a JVM only module that depends on the KMP one.

group = "com.takaotech.ktravel"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.cio.EngineMain"
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(projects.gunzouNavigator)
    // :gunzou-navigator declares Ktor as implementation, so the engine is on the runtime classpath
    // but not on this module's compile classpath, where EngineMain is referenced.
    implementation(ktorLibs.server.cio)
    // SLF4J binding: Kermit is routed into SLF4J on the JVM, so logback.xml owns the output format.
    implementation(libs.logback.classic.server)

    testImplementation(libs.kotlin.test)
    testImplementation(ktorLibs.server.testHost)
}

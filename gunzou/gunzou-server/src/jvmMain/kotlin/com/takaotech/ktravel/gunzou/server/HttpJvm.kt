package com.takaotech.ktravel.gunzou.server

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.routing

/**
 * The HTTP features that only exist on the JVM: `ktor-server-compression` and `ktor-server-swagger`
 * publish no native variant.
 *
 * The document Swagger UI renders is generated from the routing tree — see [navigatorApiDocument] —
 * rather than read from a file on the classpath. Nothing is written to disk and nothing has to be
 * kept in step by hand: `/swagger` is the UI and `/swagger/documentation.yaml` is the specification,
 * both built on the first request from the routes this application actually installed.
 *
 * `ktor-server-openapi` is deliberately not installed alongside it. It renders a second, static copy
 * of the same document through swagger-codegen, which costs a code generation pass at every startup
 * and writes the result into a `docs/` directory relative to the working directory — inside the
 * repository, when the server is started from Gradle.
 *
 * @param config The deployment being documented; its version is the one the document reports.
 */
fun Application.configureHttpJvm(config: NavigatorServerConfig) {
    install(Compression)
    routing {
        swaggerUI(path = "swagger") {
            navigatorApiDocument(config.version)

            source = OpenApiDocSource.Routing(contentType = ContentType.Application.Yaml)
        }
    }
}

package com.takaotech.ktravel

import com.takaotech.navigator.api.NavigatorApi
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiDoc
import io.ktor.openapi.Operation
import io.ktor.openapi.ReferenceOr
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val TEST_VERSION = "1.2.3"

/**
 * The OpenAPI document, which is generated from the routing tree rather than written by hand.
 *
 * These tests exist because generating it moves the failure rather than removing it: a hand written
 * specification drifts silently and is caught by whoever tries to use it, while a generated one
 * breaks where a route stops carrying what the document needs. Only a test turns that into a build
 * failure instead of a surprise in someone's client generator.
 */
class OpenApiDocumentTest {

    /**
     * The document of the embedded server, which mounts every route the contract has.
     *
     * The application has to be started first: the routing tree the document is read from is built
     * when the modules are loaded, and before that there is nothing to describe.
     */
    private suspend fun ApplicationTestBuilder.generateDocument(): OpenApiDoc {
        var doc: OpenApiDoc? = null

        application {
            module()
            // Inside the module block the routes of this module are installed, which is all the
            // generator reads now that no security scheme is registered.
            monitor.subscribe(io.ktor.server.application.ApplicationStarted) {
                doc = it.navigatorOpenApiDoc(TEST_VERSION)
            }
        }
        startApplication()

        return assertNotNull(doc, "the application started without producing a document")
    }

    /** The single operation mounted at [path], whichever method serves it. */
    private fun OpenApiDoc.operationAt(path: String): Operation {
        val item = assertNotNull(paths[path], "no path item for $path")
        val pathItem = (item as ReferenceOr.Value).value
        return assertNotNull(pathItem.post ?: pathItem.get, "no operation on $path")
    }

    @Test
    fun `Given the routing tree When the document is generated Then it carries every contract path`() =
        testApplication {
            val paths = generateDocument().paths.keys

            assertEquals(
                setOf(
                    NavigatorApi.HEALTH,
                    NavigatorApi.PROFILES,
                    NavigatorApi.HERE_ROUTING_TEMPLATE,
                    NavigatorApi.HERE_TRANSIT,
                ),
                paths,
            )
        }

    @Test
    fun `Given a routing path When its operation is read Then it declares the body and the failures`() =
        testApplication {
            val operation = generateDocument().operationAt(NavigatorApi.HERE_ROUTING_TEMPLATE)

            assertNotNull(operation.requestBody, "the routing endpoints take a body")
            assertTrue(
                operation.parameters.orEmpty().any {
                    (it as? ReferenceOr.Value)?.value?.name == NavigatorApi.PROVIDER_KEY_HEADER
                },
                "the provider key header is part of every routing call",
            )

            val statuses = operation.responses?.responses?.keys.orEmpty()
            assertTrue(
                statuses.containsAll(
                    listOf(
                        HttpStatusCode.OK.value,
                        HttpStatusCode.BadRequest.value,
                        HttpStatusCode.Unauthorized.value,
                        HttpStatusCode.UnprocessableEntity.value,
                        HttpStatusCode.TooManyRequests.value,
                        HttpStatusCode.BadGateway.value,
                    ),
                ),
                "the failures a client branches on are missing from the document: $statuses",
            )

            // Each failure says which ErrorCode it carries, not the reason phrase of its status: a
            // `401` is three different remedies and only the code in the body distinguishes them.
            val unauthorized = operation.responses
                ?.responses
                ?.get(HttpStatusCode.Unauthorized.value)
                .let { assertNotNull(it) as ReferenceOr.Value }
                .value

            assertTrue(
                unauthorized.description.contains("MISSING_CREDENTIALS"),
                "the 401 is described as '${unauthorized.description}' rather than in the contract's terms",
            )
        }

    // AUTH DISABLED: what the document said while the routes sat under an `authenticate` block. It
    // references `NAVIGATOR_AUTH`, which no longer exists, so `@Ignore` would not compile.
    // @Test
    // fun `Given the authenticated routes When the document is generated Then only they require the token`() =
    //     testApplication {
    //         val doc = generateDocument()
    //
    //         // Inferred from the `authenticate` block the routes sit under, not declared by hand.
    //         assertTrue(
    //             doc.operationAt(NavigatorApi.HERE_ROUTING_TEMPLATE).security.orEmpty().any { NAVIGATOR_AUTH in it },
    //             "the routing paths are the ones behind the access token",
    //         )
    //         assertTrue(
    //             doc.operationAt(NavigatorApi.HEALTH).security.isNullOrEmpty(),
    //             "a health probe that has to hold a token reports outages the server does not have",
    //         )
    //         assertTrue(
    //             doc.components?.securitySchemes.orEmpty().containsKey(NAVIGATOR_AUTH),
    //             "the bearer scheme is inferred from the installed provider",
    //         )
    //     }

    // The property that replaces it: with the provider switched off, nothing in the document asks a
    // caller for a credential the server no longer checks.
    @Test
    fun `Given authentication is disabled When the document is generated Then no operation requires a token`() =
        testApplication {
            val doc = generateDocument()

            assertTrue(
                doc.operationAt(NavigatorApi.HERE_ROUTING_TEMPLATE).security.isNullOrEmpty(),
                "the routing paths sit under no `authenticate` block any more",
            )
            assertTrue(
                doc.operationAt(NavigatorApi.HEALTH).security.isNullOrEmpty(),
                "a health probe that has to hold a token reports outages the server does not have",
            )
            assertTrue(
                doc.components?.securitySchemes.isNullOrEmpty(),
                "there is no installed provider left for a scheme to be inferred from",
            )
        }

    @Test
    fun `Given the document When it is generated Then the schemas come from the contract's serializers`() =
        testApplication {
            val doc = generateDocument()

            assertEquals("gunzo-navigator", doc.info.title)
            assertEquals(TEST_VERSION, doc.info.version)

            val schemas = doc.components?.schemas.orEmpty()
            assertTrue(
                schemas.containsKey("RouteResponse") && schemas.containsKey("ErrorResponse"),
                "the inferred schemas are missing: ${schemas.keys}",
            )
            // The field the hand written YAML had drifted away from. Inference cannot lose it,
            // because it reads the same descriptor the endpoint encodes through.
            assertTrue(
                schemas["ProviderCatalogResponse"]?.properties.orEmpty().containsKey("version"),
                "the catalog schema does not describe the type the endpoint answers with",
            )
        }
}

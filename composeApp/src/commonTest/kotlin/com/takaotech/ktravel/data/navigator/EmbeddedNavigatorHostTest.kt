package com.takaotech.ktravel.data.navigator

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * The host starts a real server on a real port, so these run on a real dispatcher: a socket is not
 * something a virtual clock can stand in for.
 *
 * What is worth pinning here is not that the server works — that is tested where the server lives —
 * but the two things the host itself is responsible for: starting exactly one however many callers
 * arrive at once, and coming back on a new port after a restart.
 */
class EmbeddedNavigatorHostTest :
    BehaviorSpec({

        given("an embedded navigator host") {
            `when`("the base URL is asked for") {
                then("it points at the loopback address and a port that was really bound") {
                    withContext(Dispatchers.Default) {
                        val host = EmbeddedNavigatorHost()

                        try {
                            val baseUrl = host.baseUrl()

                            baseUrl shouldStartWith "http://127.0.0.1:"
                            baseUrl.substringAfterLast(':').toInt() shouldNotBe 0
                        } finally {
                            host.stop()
                        }
                    }
                }
            }

            `when`("it is asked for twice") {
                then("the same server answers both times") {
                    withContext(Dispatchers.Default) {
                        val host = EmbeddedNavigatorHost()

                        try {
                            host.baseUrl() shouldBe host.baseUrl()
                        } finally {
                            host.stop()
                        }
                    }
                }
            }

            // Two screens asking for a route at the same moment must not race into two servers,
            // each holding its own socket, with only one of them ever reachable afterwards.
            `when`("several callers ask at the same moment") {
                then("they all get the one server rather than starting several") {
                    withContext(Dispatchers.Default) {
                        val host = EmbeddedNavigatorHost()

                        try {
                            val urls = coroutineScope {
                                List(8) { async { host.baseUrl() } }.awaitAll()
                            }

                            urls.distinct().size shouldBe 1
                        } finally {
                            host.stop()
                        }
                    }
                }
            }

            // What happens on iOS every time the app comes back to the foreground: the socket did
            // not survive suspension, and the port that worked before is refused.
            `when`("it is restarted") {
                then("it comes back on a different port") {
                    withContext(Dispatchers.Default) {
                        val host = EmbeddedNavigatorHost()

                        try {
                            val before = host.baseUrl()
                            val after = host.restart()

                            after shouldStartWith "http://127.0.0.1:"
                            after shouldNotBe before
                        } finally {
                            host.stop()
                        }
                    }
                }
            }

            `when`("it is stopped and asked again") {
                then("a new server is started rather than the dead one handed back") {
                    withContext(Dispatchers.Default) {
                        val host = EmbeddedNavigatorHost()

                        try {
                            val before = host.baseUrl()
                            host.stop()

                            host.baseUrl() shouldNotBe before
                        } finally {
                            host.stop()
                        }
                    }
                }
            }

            `when`("it is stopped without ever having been started") {
                then("nothing happens and nothing fails") {
                    withContext(Dispatchers.Default) {
                        EmbeddedNavigatorHost().stop()
                    }
                }
            }
        }
    })

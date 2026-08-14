package com.takaotech.ktravel.data.navigator

import com.takaotech.ktravel.domain.model.AppSettingsDomain
import com.takaotech.ktravel.domain.model.TravelSettingsDomain
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import com.takaotech.ktravel.domain.repository.SettingsRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val EMBEDDED_URL = "http://127.0.0.1:54213"
private const val APP_URL = "https://nav.example.com"
private const val PLAN_URL = "https://other.example.com"

private class FakeAppSettings(initial: AppSettingsDomain) : AppSettingsRepository {
    private val state = MutableStateFlow(initial)
    override val settings: StateFlow<AppSettingsDomain> = state
    override suspend fun updateNavigatorRemote(baseUrl: String) = error("Not written here")
}

private class FakePlanSettings(override val settings: TravelSettingsDomain) : SettingsRepository {
    override suspend fun updateHereApiKey(apiKey: String) = error("Not written here")
    override suspend fun updateNavigatorSettings(preference: NavigatorKind, remoteBaseUrl: String) =
        error("Not written here")
}

private fun resolverOver(app: AppSettingsDomain, plan: TravelSettingsDomain): NavigatorTargetResolver {
    val host = mock<EmbeddedNavigatorHost> {
        everySuspend { baseUrl() } returns EMBEDDED_URL
        everySuspend { restart() } returns EMBEDDED_URL
    }
    return NavigatorTargetResolver(FakeAppSettings(app), FakePlanSettings(plan), host)
}

/**
 * Which navigator a call goes to, out of three sources that each answer a different question.
 *
 * Two rules are worth protecting. The plan's address wins over the installation's, and nothing the
 * app resolves ever carries a credential: a navigator is reached anonymously, and the only key in
 * play is the trip's provider key, which the routing request attaches itself.
 */
class NavigatorTargetResolverTest :
    BehaviorSpec({

        given("an installation with a remote navigator and a plan that adds nothing") {
            val app = AppSettingsDomain(navigatorRemoteBaseUrl = APP_URL)

            `when`("the remote is resolved") {
                then("it is the installation's address") {
                    val target = resolverOver(app, TravelSettingsDomain()).resolve(NavigatorKind.REMOTE)

                    target.baseUrl shouldBe APP_URL
                }

                then("no credential is attached to it") {
                    // The app authenticates to no navigator. The only key that travels is the trip's
                    // provider key, and the routing call carries that, not the destination.
                    resolverOver(app, TravelSettingsDomain())
                        .resolve(NavigatorKind.REMOTE).accessToken.shouldBeNull()
                }
            }

            `when`("the embedded one is resolved") {
                then("it is the loopback server") {
                    val target = resolverOver(app, TravelSettingsDomain()).resolve(NavigatorKind.EMBEDDED)

                    target.baseUrl shouldBe EMBEDDED_URL
                    target.accessToken.shouldBeNull()
                }
            }
        }

        given("a plan that names a navigator of its own") {
            val app = AppSettingsDomain(navigatorRemoteBaseUrl = APP_URL)
            val plan = TravelSettingsDomain(navigatorRemoteBaseUrl = PLAN_URL)

            `when`("the remote is resolved") {
                then("the plan's address wins over the installation's") {
                    val target = resolverOver(app, plan).resolve(NavigatorKind.REMOTE)

                    target.baseUrl shouldBe PLAN_URL
                }
            }
        }

        given("nothing configured anywhere") {
            `when`("the remote is asked about") {
                then("it is not offered") {
                    resolverOver(AppSettingsDomain(), TravelSettingsDomain()).isRemoteConfigured() shouldBe false
                }
            }

            `when`("the embedded one is asked for") {
                then("it still answers, because it always exists") {
                    resolverOver(AppSettingsDomain(), TravelSettingsDomain())
                        .resolve(NavigatorKind.EMBEDDED).baseUrl shouldBe EMBEDDED_URL
                }
            }
        }

        given("a plan whose only navigator is its own") {
            val plan = TravelSettingsDomain(navigatorRemoteBaseUrl = PLAN_URL)

            `when`("the remote is asked about") {
                then("it is offered, even though the installation has none") {
                    resolverOver(AppSettingsDomain(), plan).isRemoteConfigured() shouldBe true
                }
            }
        }

        given("a plan that prefers the remote") {
            `when`("its default is read") {
                then("that is where the transport screen opens") {
                    val plan = TravelSettingsDomain(navigatorPreference = NavigatorKind.REMOTE)

                    resolverOver(AppSettingsDomain(), plan).defaultKind() shouldBe NavigatorKind.REMOTE
                }
            }
        }

        given("an embedded server that went away while the app was suspended") {
            `when`("it is recovered") {
                then("it is restarted and answers on whatever port it got") {
                    val target = resolverOver(AppSettingsDomain(), TravelSettingsDomain())
                        .recover(NavigatorKind.EMBEDDED)

                    target.baseUrl shouldBe EMBEDDED_URL
                }
            }
        }

        given("a remote navigator that did not answer") {
            val app = AppSettingsDomain(navigatorRemoteBaseUrl = APP_URL)

            `when`("recovery is attempted") {
                then("nothing is restarted and the same address comes back") {
                    // There is nothing to restart on the other side of the network, which is why the
                    // routing service does not retry a remote call.
                    val target = resolverOver(app, TravelSettingsDomain()).recover(NavigatorKind.REMOTE)

                    target.baseUrl shouldBe APP_URL
                }
            }
        }
    })

package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.data.datasource.AppSettingsStorageDataSourceImpl
import com.takaotech.ktravel.data.storage.DatabaseProvider
import com.takaotech.ktravel.testutil.tempdir
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.path
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * The installation's preferences, against a real database.
 *
 * Against a real one rather than a fake because the two things worth checking are both properties of
 * the storage: that an absent document reads as defaults instead of failing, and that a write
 * survives the repository being rebuilt — which is the whole reason these are not held in memory.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppSettingsRepositoryTest :
    BehaviorSpec({
        val tempDir = tempdir("test-app-settings")

        lateinit var provider: DatabaseProvider
        lateinit var dataSource: AppSettingsStorageDataSourceImpl

        beforeTest {
            val testDir = (tempDir / it.name.name.replace(Regex("\\W+"), "_"))
                .also { dir -> dir.createDirectories() }
            provider = DatabaseProvider(
                databaseName = "test-app-settings",
                directory = testDir.path,
                scope = TestScope(UnconfinedTestDispatcher()),
            )
            dataSource = AppSettingsStorageDataSourceImpl(provider)
        }

        afterTest {
            try {
                provider.database.close()
            } catch (e: Exception) {
                // Already closed by the test that opened it.
            }
        }

        given("an installation that has never been configured") {
            `when`("the preferences are read") {
                then("they are the defaults, and no remote navigator is offered") {
                    val repository = AppSettingsRepositoryImpl(dataSource)

                    repository.settings.value.navigatorRemoteBaseUrl shouldBe ""
                    repository.settings.value.hasRemoteNavigator shouldBe false
                }
            }
        }

        given("a remote navigator being configured") {
            `when`("its address is saved") {
                then("the flow reports it at once") {
                    val repository = AppSettingsRepositoryImpl(dataSource)

                    repository.updateNavigatorRemote("https://nav.example.com")

                    repository.settings.value.navigatorRemoteBaseUrl shouldBe "https://nav.example.com"
                    repository.settings.value.hasRemoteNavigator shouldBe true
                }

                then("a repository built afterwards starts from it") {
                    AppSettingsRepositoryImpl(dataSource).updateNavigatorRemote("https://nav.example.com")

                    // The point of storing it: the choice has to survive the process, not the screen
                    // that made it.
                    AppSettingsRepositoryImpl(dataSource)
                        .settings.value.navigatorRemoteBaseUrl shouldBe "https://nav.example.com"
                }

                then("surrounding whitespace is dropped") {
                    val repository = AppSettingsRepositoryImpl(dataSource)

                    // An address pasted from a browser arrives with it often enough that the first
                    // symptom would otherwise be a failure nobody can explain.
                    repository.updateNavigatorRemote("  https://nav.example.com  ")

                    repository.settings.value.navigatorRemoteBaseUrl shouldBe "https://nav.example.com"
                }
            }

            `when`("the address is later cleared") {
                then("the remote stops being offered") {
                    val repository = AppSettingsRepositoryImpl(dataSource)
                    repository.updateNavigatorRemote("https://nav.example.com")

                    repository.updateNavigatorRemote("")

                    repository.settings.value.hasRemoteNavigator shouldBe false
                }
            }
        }
    })

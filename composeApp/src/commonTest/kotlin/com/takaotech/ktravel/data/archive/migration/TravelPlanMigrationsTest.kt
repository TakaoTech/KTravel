package com.takaotech.ktravel.data.archive.migration

import com.takaotech.ktravel.data.archive.TravelArchiveFormat
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

/**
 * Guards the registry itself: raising `CURRENT_SCHEMA_VERSION` without registering the matching step
 * would otherwise only surface when a user fails to import an old archive.
 */
class TravelPlanMigrationsTest : BehaviorSpec({

    given("the shipped migration registry") {

        `when`("the chain covering every supported version is requested") {
            val chain = TravelPlanMigrations.migrationsFrom(
                fromVersion = TravelArchiveFormat.MIN_SUPPORTED_SCHEMA_VERSION,
                toVersion = TravelArchiveFormat.CURRENT_SCHEMA_VERSION
            )

            then("it holds one step per version, contiguous from the oldest supported one") {
                chain.map { it.fromVersion } shouldBe (
                        TravelArchiveFormat.MIN_SUPPORTED_SCHEMA_VERSION until
                                TravelArchiveFormat.CURRENT_SCHEMA_VERSION
                        ).toList()
            }
        }

        `when`("the chain from the current version is requested") {
            val chain = TravelPlanMigrations.migrationsFrom(
                fromVersion = TravelArchiveFormat.CURRENT_SCHEMA_VERSION,
                toVersion = TravelArchiveFormat.CURRENT_SCHEMA_VERSION
            )

            then("nothing has to be migrated") {
                chain.shouldBeEmpty()
            }
        }
    }
})

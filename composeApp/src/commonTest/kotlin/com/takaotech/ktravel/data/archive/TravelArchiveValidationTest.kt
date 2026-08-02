package com.takaotech.ktravel.data.archive

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/** Difesa contro lo zip-slip: i path del piano sono input non fidato. */
class TravelArchiveValidationTest : BehaviorSpec({

    given("a relative path coming from an archive") {

        `when`("it follows the travelId/stepId/fileName scheme") {
            then("it is accepted") {
                TravelArchiveValidation.isSafeRelativePath("t1/s1/abc.jpg") shouldBe true
                TravelArchiveValidation.isSafeRelativePath(
                    "0f8e-4a2b/9c1d-77ef/1a2b3c.png"
                ) shouldBe true
            }
        }

        `when`("it tries to escape the attachments root") {
            then("it is rejected") {
                TravelArchiveValidation.isSafeRelativePath("../../evil.txt") shouldBe false
                TravelArchiveValidation.isSafeRelativePath("t1/../s1/x.jpg") shouldBe false
                TravelArchiveValidation.isSafeRelativePath("/etc/passwd") shouldBe false
                TravelArchiveValidation.isSafeRelativePath("t1\\s1\\x.jpg") shouldBe false
            }
        }

        `when`("its shape does not match the scheme") {
            then("it is rejected") {
                TravelArchiveValidation.isSafeRelativePath("") shouldBe false
                TravelArchiveValidation.isSafeRelativePath("t1/s1") shouldBe false
                TravelArchiveValidation.isSafeRelativePath("t1/s1/sub/x.jpg") shouldBe false
                TravelArchiveValidation.isSafeRelativePath("t1//x.jpg") shouldBe false
            }
        }
    }
})

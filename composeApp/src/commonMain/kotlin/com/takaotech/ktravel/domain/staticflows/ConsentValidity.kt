package com.takaotech.ktravel.domain.staticflows

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * How long an answer to the privacy policy stands before it is asked again.
 *
 * A year, counted from the moment the user answered. Consent that was given once and never revisited
 * stops meaning much: the application changes, what it sends changes with it, and an answer given to
 * a version nobody remembers is not an informed one.
 */
val CONSENT_VALIDITY: Duration = 365.days

/**
 * Whether an answer given at [decidedAt] has expired by [now].
 *
 * An answer that has expired counts as no answer at all: the privacy page is shown again and, until it is
 * answered, nothing is sent.
 *
 * @param decidedAt When the user last answered.
 * @param now The moment being judged against.
 */
fun isConsentExpired(decidedAt: Instant, now: Instant): Boolean = now - decidedAt >= CONSENT_VALIDITY

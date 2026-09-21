package com.takaotech.ktravel.data.staticflows

import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.staticflows.IntroFlow
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicy
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The introduction and the privacy policy, read once per language and then remembered.
 *
 * Neither of the two callers can be the owner: navigation, to decide what is due before it picks the
 * first screen, and the screens themselves, to draw it. Reading both files again on every launch to
 * answer the same question would be the alternative.
 *
 * @param introDataSource Reads the packaged introduction.
 * @param privacyDataSource Reads the packaged privacy policy.
 */
@SingleIn(AppScope::class)
@Inject
class StaticContentRepository(
    private val introDataSource: IntroFlowDataSource,
    private val privacyDataSource: PrivacyPolicyDataSource,
) {

    private val introCache = mutableMapOf<String, IntroFlow>()

    private val privacyCache = mutableMapOf<String, PrivacyPolicy>()

    private val lock = Mutex()

    /**
     * The introduction in [language], from the cache when it has already been read.
     *
     * @param language A two letter language code, the device's own.
     */
    suspend fun introFlow(language: String): IntroFlow = lock.withLock {
        introCache.getOrPut(language) { introDataSource.load(language) }
    }

    /**
     * The privacy policy in [language], from the cache when it has already been read.
     *
     * @param language A two letter language code, the device's own.
     */
    suspend fun privacyPolicy(language: String): PrivacyPolicy = lock.withLock {
        privacyCache.getOrPut(language) { privacyDataSource.load(language) }
    }
}

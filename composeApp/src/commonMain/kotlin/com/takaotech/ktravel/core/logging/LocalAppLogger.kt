package com.takaotech.ktravel.core.logging

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The application's [AppLogger], for the composables that log without a presenter behind them —
 * a link that will not open, say, where the failure only exists in the UI.
 *
 * `App` provides the one the dependency graph binds. Outside it — a preview, a UI test — the default
 * discards every line instead of failing: a missing logger is not worth a crash, and a composable
 * that logs renders there without any setup.
 *
 * Static because it never changes while the application runs.
 */
val LocalAppLogger = staticCompositionLocalOf<AppLogger> { DiscardingAppLogger }

/** The [AppLogger] of a composition nobody provided one to: every line is dropped. */
private object DiscardingAppLogger : AppLogger {

    override fun withTag(tag: String): AppLogger = this

    override fun v(throwable: Throwable?, message: () -> String) = Unit

    override fun d(throwable: Throwable?, message: () -> String) = Unit

    override fun i(throwable: Throwable?, message: () -> String) = Unit

    override fun w(throwable: Throwable?, message: () -> String) = Unit

    override fun e(throwable: Throwable?, message: () -> String) = Unit
}

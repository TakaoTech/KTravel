package com.takaotech.ktravel.core.annotation

/**
 * Annotation that designates a class as open for mocking or subclassing in testing scenarios.
 *
 * By default, Kotlin classes are final unless explicitly marked as `open`. This annotation can be
 * used to make certain classes implicitly open for testing purposes, without altering their design
 * or requiring changes in production code. Typically paired with tools like Mockery to
 * simplify testing strategies.
 *
 * Use this annotation selectively, as it bypasses Kotlin's intentional constraints on inheritance
 * to support testing frameworks.
 */
annotation class OpenForMokkery

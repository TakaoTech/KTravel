package com.takaotech.ktravel

/**
 * The build this server reports through `GET /v1/health`.
 *
 * Kept in step with the `version` declared in `gunzou/gunzou-navigator/build.gradle.kts` by hand.
 * Generating it would mean a build config task on a module that has none, for a string whose only
 * job is to make a bug report name the right build; the tradeoff is worth revisiting the first time
 * the two disagree.
 */
internal const val NAVIGATOR_VERSION: String = "1.0.0-SNAPSHOT"

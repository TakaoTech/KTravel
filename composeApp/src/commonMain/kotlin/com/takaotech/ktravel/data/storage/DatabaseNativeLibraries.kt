package com.takaotech.ktravel.data.storage

/**
 * Loads the native libraries the database engine needs but the platform does not provide.
 *
 * Must run before the first `DatabaseConfiguration` is created: on the JVM that class' static
 * initializer is what triggers `CouchbaseLite.init` and, with it, the native library load.
 *
 * Only the JVM desktop target on Linux does any work here; every other platform ships an engine
 * whose dependencies are already satisfied.
 */
internal expect fun ensureDatabaseNativeLibraries()

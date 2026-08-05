package com.takaotech.ktravel.data.storage

import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.getOperatingSystem
import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicBoolean

/**
 * libLiteCore.so declares NEEDED entries for ICU 71 (libicuuc, libicui18n, libicudata). No Ubuntu
 * LTS ships that major — 22.04 has 70, 24.04 has 74 — and ICU exports version suffixed symbols
 * (u_strlen_71), so a symlink to another major resolves the file and then fails on the symbols.
 *
 * The libraries therefore travel inside the application, packaged as resources by the
 * `fetchCouchbaseIcuLibraries` task. [System.load] maps each one under its SONAME, which is exactly
 * the name libLiteCore.so asks for; once they are mapped the dynamic linker satisfies those NEEDED
 * entries from the already-loaded set and never searches the system paths. Nothing has to be
 * installed on the machine, and LD_LIBRARY_PATH stays out of the picture.
 */
private object IcuLibraries {
    /** Dependency order: libicuuc needs libicudata, libicui18n needs both. */
    val sonames = listOf("libicudata.so.71", "libicuuc.so.71", "libicui18n.so.71")

    val loaded = AtomicBoolean(false)
}

internal actual fun ensureDatabaseNativeLibraries() {
    if (getOperatingSystem() != OperatingSystem.LINUX) return
    if (IcuLibraries.loaded.getAndSet(true)) return

    val scratchDir: File = Files.createTempDirectory("ktravel-icu").toFile()
    scratchDir.deleteOnExit()

    for (soname in IcuLibraries.sonames) {
        // Absent from a build produced on another host, since the packaging task only runs on
        // Linux. Leaving resolution to the system is the right fallback: it succeeds on a machine
        // that does provide ICU 71, and otherwise Couchbase Lite reports the missing library.
        val resource = IcuLibraries::class.java.getResourceAsStream("/couchbase/icu/$soname")
            ?: return

        val target = File(scratchDir, soname)
        resource.use { input -> target.outputStream().use(input::copyTo) }
        target.deleteOnExit()

        System.load(target.absolutePath)
    }
}

package com.takaotech.ktravel

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.platformLogWriter

// Resolves to Kermit's NSLogWriter.
internal actual fun appLogWriter(): LogWriter = platformLogWriter()

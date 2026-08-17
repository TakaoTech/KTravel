package com.takaotech.ktravel

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.platformLogWriter

actual fun appLogWriter(): LogWriter = platformLogWriter()

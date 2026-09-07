package com.takaotech.ktravel.core.logging

import co.touchlab.kermit.Logger
import com.takaotech.ktravel.core.logging.slf4j.KermitSlf4jBridge

actual fun installPlatformLogBridge(logger: Logger) = KermitSlf4jBridge.install(logger)

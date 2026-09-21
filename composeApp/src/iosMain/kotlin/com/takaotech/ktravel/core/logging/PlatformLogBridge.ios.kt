package com.takaotech.ktravel.core.logging

import co.touchlab.kermit.Logger

/** No SLF4J on iOS, so there is nothing to redirect: everything already logs through Kermit. */
actual fun installPlatformLogBridge(logger: Logger) = Unit

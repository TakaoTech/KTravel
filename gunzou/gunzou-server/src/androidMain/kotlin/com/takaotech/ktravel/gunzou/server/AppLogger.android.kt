package com.takaotech.ktravel.gunzou.server

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.platformLogWriter

actual fun appLogWriter(): LogWriter = platformLogWriter()

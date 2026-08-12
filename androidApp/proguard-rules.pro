# Application-level ProGuard/R8 rules.
#
# Library keep rules are NOT duplicated here: :composeApp and :gunzou-here-client publish
# their own consumer keep rules, and third-party AARs (Couchbase, MapLibre, OkHttp, Coil,
# kotlinx.serialization) ship theirs.

# Project decision: shrink and optimize, but never rename. Crash reports and stack traces coming
# from released builds stay readable without a mapping file, and nothing that resolves a class by
# name at runtime can break.
-dontobfuscate

# The parcelize plugin is configured with a custom annotation
# (see the additionalAnnotation compiler arg in composeApp/build.gradle.kts).
-keep class com.takaotech.ktravel.core.annotation.** { *; }

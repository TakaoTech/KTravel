# Desktop-only ProGuard rules for the :composeApp module.
#
# ProGuard on the desktop target does not read consumer rules from jars: everything that Android
# gets for free from AAR consumer rules has to be written by hand here.
#
# The Compose Gradle plugin already includes default-compose-desktop-rules.pro (kotlin.**, skia,
# skiko, coroutines, kotlinx.serialization) and generates the keep for the configured mainClass, so
# none of that is repeated below.

# ── Enums ────────────────────────────────────────────────────────────────────────────────────────
# Class.getEnumConstants() — used by EnumMap, EnumSet and by kotlinx.serialization — reaches the
# constants through the synthetic values() method and the $VALUES field. ProGuard's optimizer
# removes them, and EnumMap's constructor then fails with
# "Cannot read the array length because this.keyUniverse is null"
# (observed on com.couchbase.lite.LogLevel during static init, which aborts database startup).
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Couchbase Lite Java (JNI) ────────────────────────────────────────────────────────────────────
# couchbase-lite-android ships these rules as consumer rules; couchbase-lite-java ships none, even
# though it bundles the very same LiteCore native libraries. Copied verbatim from the AAR.
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings

-keep class com.couchbase.lite.ConnectionStatus { <init>(...); }
-keep class com.couchbase.lite.LiteCoreException { static <methods>; }
-keep class com.couchbase.lite.internal.replicator.CBLTrustManager {
    public java.util.List checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String, java.lang.String);
}
-keep interface com.couchbase.lite.internal.ReplicationCollection$C4Filter
-keep class com.couchbase.lite.internal.ReplicationCollection {
    static <methods>;
    <fields>;
}
-keep class com.couchbase.lite.internal.fleece.FLSliceResult {
    static <methods>;
    <fields>;
    <init>(...);
}
-keep class com.couchbase.lite.internal.core.C4* {
    static <methods>;
    <fields>;
    <init>(...);
}

# couchbase-lite-java is compiled against JSR-305 annotations and an OkHttp 3 internal
# (okhttp3.internal.Util) that no longer exists in the OkHttp version we resolve.
-dontwarn com.couchbase.lite.**
-dontwarn javax.annotation.**

# ── logback / SLF4J ──────────────────────────────────────────────────────────────────────────────
# SLF4J 2.x binds its backend through ServiceLoader<SLF4JServiceProvider>; the provider named in
# META-INF/services is unreachable for the shrinker.
-keep class ch.qos.logback.classic.spi.LogbackServiceProvider { *; }
-keep class ch.qos.logback.** { *; }
-keep class org.slf4j.** { *; }
-dontwarn ch.qos.logback.**
-dontwarn javax.servlet.**
-dontwarn jakarta.servlet.**

# ── Ktor OkHttp engine ───────────────────────────────────────────────────────────────────────────
# Loaded via META-INF/services/io.ktor.client.HttpClientEngineContainer.
-keep class io.ktor.client.engine.okhttp.OkHttpEngineContainer { *; }
-keep class io.ktor.client.engine.okhttp.** { *; }
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
# OkHttp ships GraalVM native-image substitutions that reference the Graal SDK, which is not on
# our classpath and is never loaded on a plain JVM.
-dontwarn okhttp3.internal.graal.**

# ── Circuit codegen annotations ──────────────────────────────────────────────────────────────────
# @CircuitInject is meta-annotated with kotlin-inject-anvil and Hilt markers that are compile-only.
-dontwarn com.slack.circuit.codegen.annotations.**
-dontwarn amazon.lastmile.inject.**

# ── JNA (pulled in by filekit-core / filekit-dialogs) ─────────────────────────────────────────────
# Structure subclasses have their field order read reflectively, so members must survive.
-keep class com.sun.jna.** { *; }
-keepclassmembers class * extends com.sun.jna.** { public *; }
-dontwarn com.sun.jna.**

# ── MapLibre Native FFI (desktop map) ────────────────────────────────────────────────────────────
# The desktop map talks to MapLibre Native through the FFM API: downcall handles and struct layouts
# are built by name, and the GPU presenter is chosen at runtime from the render backend the runtime
# artifact provides (Metal on macOS, Vulkan elsewhere). None of that is visible to the shrinker.
-keep class org.maplibre.compose.desktop.** { *; }
-keep class org.maplibre.compose.mlnffi.** { *; }
-keep class org.maplibre.nativeffi.** { *; }

# Every downcall goes through MethodHandle.invokeExact, which is signature polymorphic: the JDK
# declares a single invokeExact(Object...) and the compiler emits a call site carrying the real
# descriptor. ProGuard resolves members by descriptor and cannot match those, so it reports one
# "can't find referenced method" per stub — 47 of them, all expected and all harmless.
-dontwarn org.maplibre.nativeffi.**

# LWJGL loads its own native libraries through Configuration / Class.forName, and reads the field
# order of its Struct subclasses reflectively.
-keep class org.lwjgl.** { *; }
-keepclassmembers class * extends org.lwjgl.system.Struct { *; }
-dontwarn org.lwjgl.**

# ── AWT / Swing ──────────────────────────────────────────────────────────────────────────────────
-dontwarn java.awt.**
-dontwarn javax.swing.**

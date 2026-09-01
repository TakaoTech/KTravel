# Consumer ProGuard/R8 rules for the :gunzou-server module.
#
# These rules are published with the Android variant and are applied automatically to any consumer
# that minifies (see androidApp). The desktop build cannot read consumer rules, so this same file is
# referenced explicitly from composeApp's `compose.desktop` ProGuard configuration.
#
# The application embeds this server in its own process, so everything a Ktor server resolves by
# name at runtime has to survive the shrinker — and a server that fails to start does so only in a
# release build, which is the worst place to find out.

# Ktor resolves engines and plugins through service loaders and reflection.
-keep class io.ktor.server.cio.** { *; }
-keep class io.ktor.server.engine.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-dontwarn io.ktor.**

# Koin instantiates the definitions of the server module by constructor reference.
-keep class org.koin.core.** { *; }
-dontwarn org.koin.**

# The contract DTOs are kept by :gunzou-api's own consumer rules. What is left here is the
# server's own serialisable surface, which the generated serialisers reach by name.
-keepclassmembers class com.takaotech.ktravel.gunzou.server.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.takaotech.ktravel.gunzou.server.**$$serializer { *; }

# Kermit picks its writer per platform; the JVM one goes through SLF4J, which is resolved by
# service loader.
-dontwarn org.slf4j.**

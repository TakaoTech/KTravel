# Consumer ProGuard/R8 rules for the :composeApp module.
#
# These rules are published with the Android variant and applied automatically to androidApp. The
# desktop build cannot read consumer rules, so this same file is referenced explicitly from the
# `compose.desktop` ProGuard configuration in build.gradle.kts.

# Required by kotlinx.serialization, and by readable stack traces in crash reports.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepattributes SourceFile, LineNumberTable

# Type-safe navigation routes (App.kt and ui/**) are @Serializable data classes; NavHost and
# toRoute<T>() resolve their serializer from the KClass at runtime. The same applies to the sealed
# StepEntity hierarchy in data/entity and to the archive manifest in data/archive.
-keepclassmembers class com.takaotech.ktravel.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.takaotech.ktravel.**$$serializer { *; }

# Generated Compose Resources accessor (namespace com.takaotech.ktravel.compose).
-keep class com.takaotech.ktravel.compose.generated.resources.** { *; }

# org.maplibre.compose:location, an api dependency of maplibre-compose, declares @Serializable
# models (Location, Orientation, the *WithAccuracy types) whose Companion / $$serializer are
# resolved by name at runtime, and ships no consumer rules of its own. Everything else MapLibre
# needs from the shrinker is covered upstream: maplibre-compose-android keeps the Vulkan JNI
# bridge, and maplibre-native-ffi-android keeps the JavaCPP loader.
-keepclassmembers class org.maplibre.compose.location.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class org.maplibre.compose.location.**$$serializer { *; }

# Compottie parses Lottie JSON with its own internal @Serializable model.
-keepclassmembers class io.github.alexzhirkevich.compottie.internal.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class io.github.alexzhirkevich.compottie.internal.**$$serializer { *; }

# OkHttp references these TLS providers reflectively; they are never on the runtime classpath.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# The SLF4J backend of the application (core/logging/slf4j), resolved through
# ServiceLoader<SLF4JServiceProvider> from META-INF/services and therefore invisible to R8.
-keep class com.takaotech.ktravel.core.logging.slf4j.KermitSlf4jServiceProvider { *; }

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

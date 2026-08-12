# Consumer ProGuard/R8 rules for the :gunzou-here-client module.
#
# These rules are published with the Android variant of the module and are applied automatically to
# any consumer that minifies (see androidApp). The desktop build cannot read consumer rules, so this
# same file is referenced explicitly from composeApp's `compose.desktop` ProGuard configuration.

# Required by kotlinx.serialization to resolve generic type arguments and the @Serializable
# annotation itself at runtime.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# The HERE routing and public transit DTOs are @Serializable. kotlinx.serialization looks up the
# generated Companion / $$serializer by name, so the shrinker cannot see those references.
-keepclassmembers class com.takaotech.navigation.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.takaotech.navigation.**$$serializer { *; }

# OkHttp references these TLS providers reflectively; they are never on the runtime classpath.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

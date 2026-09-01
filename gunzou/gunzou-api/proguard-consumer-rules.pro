# Consumer ProGuard/R8 rules for the :gunzou-api module.
#
# These rules are published with the Android variant of the module and are applied automatically to
# any consumer that minifies (see androidApp). The desktop build cannot read consumer rules, so this
# same file is referenced explicitly from composeApp's `compose.desktop` ProGuard configuration.

# Required by kotlinx.serialization to resolve generic type arguments and the @Serializable
# annotation itself at runtime.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# The whole contract is @Serializable. kotlinx.serialization looks up the generated Companion /
# $$serializer by name, so the shrinker cannot see those references.
-keepclassmembers class com.takaotech.gunzou.api.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.takaotech.gunzou.api.**$$serializer { *; }

# Sealed hierarchies are resolved by their @SerialName discriminator, which means the subclasses are
# only ever reached reflectively through the generated sealed serializer.
-keep class com.takaotech.gunzou.api.common.RouteTime { *; }
-keep class com.takaotech.gunzou.api.common.RouteTime$* { *; }

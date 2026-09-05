# Consumer ProGuard/R8 rules for the :gunzou-client module.
#
# These rules are published with the Android variant of the module and are applied automatically to
# any consumer that minifies (see androidApp). The desktop build cannot read consumer rules, so this
# same file is referenced explicitly from composeApp's `compose.desktop` ProGuard configuration.
#
# The contract types themselves are kept by :gunzou-api's own consumer rules; what is left
# here is what the HTTP engines need.

# OkHttp references these TLS providers reflectively; they are never on the runtime classpath.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

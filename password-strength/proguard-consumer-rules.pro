# Consumer ProGuard/R8 rules for the :password-strength module.
#
# These rules are published with the Android variant of the module and are applied automatically to
# any consumer that minifies (see androidApp). The desktop build cannot read consumer rules, so this
# same file is referenced explicitly from composeApp's `compose.desktop` ProGuard configuration.

# The module carries no reflection, no serialization and no JNI: everything is resolved statically
# at compile time, so the shrinker sees every reference it needs. Nothing to keep.
#
# The generated dictionary holders are plain `internal` objects with `const val` chunks. R8 inlines
# the constants and drops the holders, which is the desired outcome — do not add a -keep for them.

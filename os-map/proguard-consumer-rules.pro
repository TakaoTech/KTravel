# Consumer ProGuard/R8 rules for the :os-map module.
#
# The MapLibre Android SDK ships its own consumer rules (Gson, org.maplibre.geojson, enum values()),
# so only what those rules do not cover belongs here.

# maplibre-compose declares @Serializable location models whose Companion / $$serializer are
# resolved by name at runtime.
-keepclassmembers class org.maplibre.compose.location.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class org.maplibre.compose.location.**$$serializer { *; }

# The desktop map (Mapsforge) is not on the Android classpath.
-dontwarn org.mapsforge.**
-dontwarn org.kxml2.**
-dontwarn com.kitfox.svg.**

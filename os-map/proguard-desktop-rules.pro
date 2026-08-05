# Desktop-only ProGuard rules for the :os-map module.
#
# ProGuard on the desktop target does not read consumer rules from jars, so this file is referenced
# explicitly from composeApp's `compose.desktop` ProGuard configuration.
#
# The desktop map is a completely different stack from Android: MapForge.kt embeds Mapsforge inside
# a SwingPanel instead of MapLibre.

# Mapsforge parses its XML render themes through XmlPullParserFactory / Class.forName, so the
# shrinker cannot see which parser or theme classes are actually used.
-keep class org.mapsforge.** { *; }
-keep class org.kxml2.** { *; }
-keep class org.xmlpull.** { *; }
-dontwarn org.mapsforge.map.android.**

# SVG Salamander renders the theme symbols and instantiates its element classes reflectively.
-keep class com.kitfox.svg.** { *; }

# AWT / Swing interop used by the SwingPanel host.
-dontwarn java.awt.**
-dontwarn javax.swing.**

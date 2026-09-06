# iOS test binaries fail to link or abort at startup

Symptoms seen when running the shared `commonTest` suites on an iOS simulator target
(`./gradlew :composeApp:iosSimulatorArm64Test`).

## Symptom 1 — the link fails

```
> Task :composeApp:linkDebugTestIosSimulatorArm64 FAILED
e: /Applications/Xcode.app/.../usr/bin/ld invocation reported errors
Please try to disable compiler caches and rerun the build.
To disable compiler caches, use `disableNativeCache` in the binary declaration ...
output:
ld: framework 'CouchbaseLite' not found
```

The advice about compiler caches is a red herring: the real error is the last line of the linker
output, and `disableNativeCache` changes nothing here. Always read past the Kotlin/Native wrapper
message to the `ld:` line underneath it.

### Cause

The kotbase cinterop klib (`dev.kotbase:couchbase-lite`) declares the framework it needs in its own
manifest:

```
linkerOpts=-framework CouchbaseLite
modules=CouchbaseLite
package=cocoapods.CouchbaseLite
```

The application never trips over this. `composeApp` exports a **static** framework, so its
unresolved symbols are the consuming Xcode project's problem, and Xcode resolves `CouchbaseLite`
through the Swift package referenced by `iosApp.xcodeproj`. The test executables are the one Apple
binary Gradle links itself, with no Xcode around it — nothing provides the framework, and the link
fails.

### Fix

`composeApp/build.gradle.kts` downloads the Objective-C xcframework Couchbase publishes and hands it
to the linker:

- `fetchCouchbaseLiteAppleFramework` unpacks
  `couchbase-lite-objc_xc_community_<version>.zip` into `build/generated/couchbaseLiteApple`. The
  version is derived from `libs.versions.kotbase`, so the framework and the Kotlin bindings cannot
  drift apart. Only the two iOS slices are extracted: the macOS and Mac Catalyst ones are versioned
  bundles held together by symlinks, which a plain `ZipFile` reader materialises as text stubs
  holding the target path. The iOS slices are flat, so they come out intact.
- `binaries.withType<TestExecutable>()` adds `-F` pointing at the slice matching the target
  (`ios-arm64_x86_64-simulator` or `ios-arm64`), and makes the link task depend on the fetch.

This mirrors `fetchCouchbaseIcuLibraries`, which solves the equivalent problem for the Linux desktop
build (see the root `CLAUDE.md`).

## Symptom 2 — it links, then aborts at startup

```
dyld[…]: Library not loaded: @rpath/CouchbaseLite.framework/CouchbaseLite
  Referenced from: …/composeApp/build/bin/iosSimulatorArm64/debugTest/test.kexe
Child process terminated with signal 6: Abort trap
```

### Cause

`CouchbaseLite.framework` is dynamic, with an `@rpath`-relative install name:

```
$ otool -D …/CouchbaseLite.framework/CouchbaseLite
@rpath/CouchbaseLite.framework/CouchbaseLite
```

A search path (`-F`/`-L`) is enough to *link*; loading the library at run time needs an `-rpath`
entry as well, so the executable links cleanly and then dies on the first load.

### Fix

The same `TestExecutable` block adds an absolute `-rpath` entry:

```kotlin
iosTarget.binaries.withType<TestExecutable>().configureEach {
    val couchbaseDir = couchbaseLiteFrameworkDir(iosTarget.name).absolutePath
    linkerOpts(*MAPLIBRE_IOS_LINKER_FLAGS)
    linkerOpts("-F$couchbaseDir", "-rpath", couchbaseDir)
    linkTaskProvider.configure { dependsOn(fetchCouchbaseLiteAppleFramework) }
}
```

Absolute paths into the build directory are fine here: the simulator runs on the host filesystem,
and these binaries are never distributed.

## Symptom 3 — undefined C++, ImageIO or Metal symbols

```
Undefined symbols for architecture arm64:
  "std::__1::__throw_system_error(int, char const*)", referenced from: …
  "_CGImageSourceCreateWithData", referenced from: …
```

### Cause

Since maplibre-compose 0.15.0 the iOS map is MapLibre Native FFI, shipped as a **static** library
inside the klib — there is no `MapLibre.framework` Swift package any more, and no `spmForKmp` plugin
in the build. A static library records no link dependencies of its own, so the system libraries it
was compiled against have to be named by whatever produces the executable.

### Fix

`MAPLIBRE_IOS_LINKER_FLAGS` in `composeApp/build.gradle.kts` holds the list from MapLibre's
[getting started guide](https://maplibre.org/maplibre-compose/getting-started/) — `-lc++`, `-lz` and
the CoreFoundation / CoreGraphics / CoreText / Foundation / ImageIO / Metal / QuartzCore frameworks.
It is applied in two places, and both have to stay in step with the guide:

- the test executables, through the `linkerOpts` above;
- the application, through `OTHER_LDFLAGS` on the app target of `iosApp/iosApp.xcodeproj` —
  `composeApp` exports a *static* framework, so Xcode is what links these symbols for the app.

A change that only touches the Gradle side will pass `iosSimulatorArm64Test` and then fail the Xcode
build, and the other way round.

## Diagnosing a new missing framework

1. Read the actual `ld:` line, not the Kotlin/Native "disable compiler caches" suggestion.
2. Find which klib asks for the framework — unzip the cinterop klib and read `default/manifest`:

   ```bash
   unzip -o -q ~/.gradle/caches/modules-2/files-2.1/<group>/<artifact>/<version>/<hash>/<name>.klib -d /tmp/klib
   cat /tmp/klib/default/manifest   # linkerOpts=… tells you what the linker is being asked for
   ```
3. Check whether the framework is static or dynamic (`file …/Foo.framework/Foo`) and read its
   install name (`otool -D …`). Dynamic plus `@rpath` means it needs `-rpath` on top of the search
   path.

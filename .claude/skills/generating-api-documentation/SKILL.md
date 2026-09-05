---
name: generating-api-documentation
description: Generate, configure and publish the aggregated Dokka API documentation for KTravel. Encodes which six modules are documented and why the other two are not, the single `dokkaAll` entry point and the trap of the unqualified `dokkaGenerate`, why the shared configuration cannot live in a `buildSrc` convention plugin (the Dokka plugin loaded there cannot see the Kotlin plugin and silently documents nothing), why `:composeApp` has to drop `src/kzipMain/kotlin` from every source root but `jvmMain`'s, and how the `Release` workflow publishes the result to GitHub Pages. Use when adding a module that should be documented, when the generated output is empty or missing modules, when changing source links or suppressions, when touching the `dokka(projects.…)` aggregation or the `docs` / `deploy-docs` jobs, or when the user mentions Dokka, `dokkaAll`, `dokkaGenerate`, `dokkaSourceSets`, `perPackageOption`, `sourceLink`, `pluginMode`, KotlinBasePlugin, GitHub Pages or "API docs".
---

# API documentation in KTravel

Dokka **2.2.0**, HTML only, driven by the Dokka Gradle Plugin v2. Every module that carries public
API reports into the root project, which merges them into one publication — the same shape the
Kover aggregation uses.

## Commands

| Goal                                | Command                                             | Output                       |
|-------------------------------------|-----------------------------------------------------|------------------------------|
| Aggregated documentation            | `./gradlew dokkaAll`                                | `build/dokka/html/index.html`|
| One module on its own               | `./gradlew :composeApp:dokkaGeneratePublicationHtml`| `composeApp/build/dokka/html`|

**Never use the unqualified `dokkaGenerate`.** Without the leading colon Gradle runs the task in
every project, so on top of the aggregate it builds a standalone publication per module that
nothing consumes. `dokkaAll` is a lifecycle alias registered in the root `build.gradle.kts` that
depends on the root project's own `:dokkaGenerate` — the aggregating one — precisely to remove that
trap. It sits next to `detektAll` and follows the same pattern.

## What is documented

The six modules with public API, listed in the root `build.gradle.kts` alongside the `kover(…)`
entries:

```kotlin
dokka(projects.composeApp)
dokka(projects.gunzouHereClient)
dokka(projects.passwordStrength)
dokka(projects.gunzouServer)
dokka(projects.gunzouApi)
dokka(projects.gunzouClient)
```

`:androidApp` and `:gunzou-server-app` are deliberately absent — they are shells (the APK entry
point and the JVM packaging module) with no public API of their own. They are excluded from Kover
for the same reason.

To document a new module: add `alias(libs.plugins.dokka)` to its `plugins { }` block **and** a
`dokka(projects.…)` line to the root aggregation. Doing only the first gives the module its own
standalone publication and leaves it out of the aggregate.

## Where the shared configuration lives, and why

In a `subprojects { plugins.withType<DokkaPlugin> { … } }` block in the root `build.gradle.kts`,
next to the aggregation. It sets, for every source set of every documented module:

- `sourceLink` to `https://github.com/TakaoTech/KTravel/tree/dev/<module path>` with `remoteLineSuffix = "#L"`;
- a `perPackageOption` suppressing `ktravel.composeapp.generated.resources`, the Compose Resources
  accessors — the very package the Kover filters exclude.

**This cannot move into a `buildSrc` convention plugin.** A convention plugin has to be compiled in
`buildSrc`, whose classloader is a parent of the build scripts' one, and the Dokka plugin loaded
there cannot see the Kotlin Gradle Plugin loaded by the scripts. `KotlinAdapter` then fails with

```
Dokka Gradle Plugin could not load KotlinBasePlugin in project ':…'
    at org.jetbrains.dokka.gradle.adapters.KotlinAdapter$Companion.findKotlinBasePlugins
```

The build still succeeds and the task still runs — it just finds no source set and emits an empty
publication (an `index.html` with module folders that contain nothing). Measured on this
repository: 0 pages through `buildSrc`, 433 pages for `:gunzou-api` alone with the plugin applied
from the version catalog. If a future change makes a convention plugin necessary anyway, the only
way out is putting the Kotlin Gradle Plugin on `buildSrc`'s own classpath, which moves the loading
of Kotlin and AGP for the whole build — not worth it here.

Applying the plugin through the catalog while `buildSrc` also exposes it fails outright:

```
Error resolving plugin [id: 'org.jetbrains.dokka', version: '2.2.0']
> The request for this plugin could not be satisfied because the plugin is already on the
  classpath with an unknown version, so compatibility cannot be checked.
```

## `:composeApp`'s own rule

`src/kzipMain/kotlin` is shared by `androidMain`, `jvmMain` and `iosMain` through `kotlin.srcDir`
(kzip has no `androidJvm` variant, so the single `actual` implementation is shared that way).
Dokka rejects a file belonging to more than one source set:

```
Pre-generation validity check failed: Source sets 'android' and 'ios' have the common source
roots: …/src/kzipMain/kotlin/…/ZipArchive.kzip.kt. Every Kotlin source file should belong to only
one source set (module).
```

The module's own `dokka { }` block therefore drops that directory from every source root but
`jvmMain`'s, so the declaration is documented exactly once:

```kotlin
dokka {
    val kzipSources = layout.projectDirectory.dir("src/kzipMain/kotlin").asFile
    dokkaSourceSets.configureEach {
        if (name != "jvmMain") {
            sourceRoots.setFrom(sourceRoots.files.filterNot { it.startsWith(kzipSources) })
        }
    }
}
```

Two things that do **not** work here, both verified: `suppressedFiles` (the pre-generation check
reads `sourceRoots`, which suppression does not touch) and comparing a source root against the
directory itself (`sourceRoots` resolves to individual files, so the filter must be a path prefix
test). Source set names are the Kotlin ones — `jvmMain`, `androidMain`, `commonMain` — not the
short `jvm` / `android` / `ios` names the error message prints.

Any other module that starts sharing a `srcDir` across source sets needs the same treatment.

## Plugin mode

`gradle.properties` pins

```properties
org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled
```

Without it the plugin runs as `V2EnabledWithHelpers` and additionally registers the deprecated V1
tasks — `dokkaHtmlMultiModule`, `dokkaHtmlCollector`, `dokkaGfm`, `dokkaJavadocCollector`. They are
easy to invoke by mistake from the IDE's Gradle panel and do not produce the aggregated
publication, which looks exactly like the aggregate having lost most of its modules.

## Publication

The `Release` workflow (`.github/workflows/release.main.kts`, YAML regenerated by running the
script) carries it:

- **`docs`** — `needs: test`, the same gate as the APK and desktop jobs. Runs `./gradlew dokkaAll`
  and uploads `build/dokka/html` with `actions/upload-pages-artifact@v5`.
- **`deploy-docs`** — `needs: docs`, holds `pages: write` and `id-token: write` alone, environment
  `github-pages`, `actions/deploy-pages@v5` with `id = "deployment"`.

So Pages always serves the documentation of the version that was just released, never an
intermediate state of the default branch. Both jobs run on Linux, where the Kotlin/Native targets
are disabled: `iosMain` is absent from the published output, while everything in `commonMain` — where
essentially all public API lives — is covered. A local run on macOS covers iOS too. Moving the job
to `macos-latest` is the fix if iOS documentation ever has to be published.

Repository setting this depends on: **Settings → Pages → Source = "GitHub Actions"**. The
`deploy-docs` job fails without it.

## Markdown / GFM is not available

Dokka v2 no longer exposes the GFM output format through a plain `dokkaPlugin` dependency: it needs
a custom `DokkaFormatPlugin` subclass, which would have to live in `buildSrc` — see the classloader
problem above — and the format is still experimental. HTML is the only format configured, and it is
also the only one that makes sense for Pages.

## Diagnosing an empty or partial output

1. `ls build/dokka/html/` — the six module folders plus `index.html`, `navigation.html`,
   `package-list`, `images/`, `scripts/`, `styles/`, `ui-kit/`.
2. Folders present but empty, or a page count in the single digits: the Kotlin plugin was not
   visible to Dokka. Check for "could not load KotlinBasePlugin" with `--info`.
3. Modules missing from `index.html`: check the module has both `alias(libs.plugins.dokka)` and its
   `dokka(projects.…)` line, and that the command was `dokkaAll` rather than a deprecated V1 task.
4. `find build/dokka/html/<module> -name '*.html' | wc -l` gives a quick per-module page count.

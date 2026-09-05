---
name: previewing-composables
description: Write Compose Multiplatform previews the way KTravel writes them — where the preview and its fixtures live (`//region Previews` inside the component file, `…Previews.kt` for a screen, `…PreviewData.kt` for `PreviewParameterProvider`s and sample values, and why those two file names are what keeps fixtures out of the coverage report), the `androidx.compose.ui.tooling.preview` imports that come from the multiplatform `ui-tooling-preview` artifact, the `private fun …Preview() = KTravelTheme { … }` shape every preview takes, when to reach for `@Preview(showBackground = true)`, `@PreviewScreenSizes`, `@PreviewLightDark` or `@PreviewFontScale`, why a preview calls the stateless `…Content` and never the `…Page`, which states a `PreviewParameterProvider` has to carry, and what detekt and Kover already do with `@Preview`. Use whenever adding or changing a `@Preview`, adding a composable that deserves one, writing preview sample data, splitting preview fixtures out of a screen file, or when a preview will not render or trips detekt.
---

# Previewing KTravel composables

Every `@Preview` in this repository lives in `composeApp/src/commonMain` — there is no preview in
`androidMain`, `jvmMain` or `iosMain`, and a new one does not start that. The renderer is the
Android one (`androidRuntimeClasspath(libs.compose.tooling)` in `composeApp/build.gradle.kts`), so
a preview is written once in shared code and drawn from the Android target of the IDE.

## When this applies

Add a preview when a composable **draws something**: a component with states, a section, a screen's
stateless content. Do not add one to plumbing — a `…Page` that only wires a ViewModel, a
`CompositionLocal` provider, a `Modifier` extension.

A composable with states the reader cannot guess (empty, error, disabled, truncated, unavailable)
needs a preview more than a well-behaved one does, because the preview is the only place those
states are ever seen together.

## Where the preview goes

Three homes, and the choice is not cosmetic:

| Situation                                                              | Home                                                             |
|------------------------------------------------------------------------|------------------------------------------------------------------|
| A component file with one or two previews and no fixtures worth naming  | Same file, in a `//region Previews` block at the bottom          |
| A screen, or a file that would grow a third preview                     | A sibling `…Previews.kt` in the same package                     |
| A `PreviewParameterProvider`, sample `val` or fixture feeding an inline preview, or shared by more than one preview file | A sibling `…PreviewData.kt` in the same package |

A fixture used only by a `…Previews.kt` may stay inside it — that file is already preview-only, and
`ReachabilityBadgePreviews.kt` is how it reads. The rule is that a fixture never sits in a
production file, not that it always gets a file of its own.

The two file suffixes are load bearing. The root `kover { reports { filters { excludes { … } } } }`
block excludes `@Preview`-annotated functions **and** the classes `*PreviewsKt` and `*PreviewDataKt`
— the file facades, which is where a top-level `val PREVIEW_CATALOG` or `fun previewSummary()` ends
up. Left in a production file those fixtures are neither annotated nor excluded, so they count
against coverage as uncovered production code, and only the file name fixes that.

A `PreviewParameterProvider` is a class of its own and matches neither pattern, so it is counted
wherever it sits. Keep it in `…PreviewData.kt` anyway — that is where the next reader looks for it,
and it is what makes a `classes("*PreviewParams")` filter possible later — but do not expect the
file name to hide it from Kover.

`preview` as a *package* name is reserved for Compose previews: shared providers live in
`ui/plan/preview/`, while the user-facing route preview feature is `ui/plan/transport/routepreview/`.
Do not reintroduce the collision.

## Imports

Always the AndroidX-shaped names, supplied by the multiplatform artifact:

```kotlin
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
```

They come from `libs.compose.preview` (`org.jetbrains.compose.ui:ui-tooling-preview`), already a
`commonMain` `implementation` dependency — nothing has to be added to the build to write a preview.
Never import `org.jetbrains.compose.ui.tooling.preview.Preview`: the Kover filter lists it for
safety, but no source in this repository uses it and mixing the two splits the annotation in half.

## The shape

```kotlin
//region Previews
@Preview(showBackground = true)
@Composable
private fun PlaceEndpointPreview(
    @PreviewParameter(PlaceEndpointPreviewParams::class) state: PlaceEndpointPreviewState,
) = KTravelTheme {
    Surface {
        PlaceEndpoint(
            modifier = Modifier
                .padding(12.dp)
                .width(240.dp),
            label = stringResource(state.label),
            name = state.name,
            icon = state.icon,
        )
    }
}
//endregion Previews
```

Fixed points:

- **`private`**, always. The one public `@Preview` is `App()` in `App.kt`, and it is public because
  it is the real entry point, not a preview of something else.
- **Name ends in `Preview`** — detekt's `Compose > PreviewNaming` requires the suffix. The rest of
  the name is the composable plus the variant: `TravelListPageSelectionModePreview`,
  `TransportStepTransitPreview`, `PlaceStepContentEmptyNotePreview`.
- **Expression body assigned to `KTravelTheme { … }`.** The theme carries the brand colours, the
  type scale and the corner radii; a preview without it draws stock Material 3 and lies about the
  result. Its own KDoc says to wrap every preview in it.
- **No `modifier` parameter** on the preview function itself. The only parameter a preview takes is
  a `@PreviewParameter`.
- **No KDoc on the preview function.** `documenting-with-kdoc` exempts `@Preview` functions; the
  fixtures behind them are what gets documented.

Component previews wrap the component in `Surface { }` and pad it, so it is not drawn edge to edge
on a transparent ground. Screen previews call the content directly — the screen brings its own
`Scaffold` and background, and a second `Surface` only adds a layer.

## Choosing the annotation

| Annotation                       | Use it for                                                                          |
|----------------------------------|--------------------------------------------------------------------------------------|
| `@Preview(showBackground = true)` | A single component: a card, a pill, a row. The default, and the most common here     |
| `@PreviewScreenSizes`             | A screen's `…Content`: the adaptive layouts are the thing being checked              |
| `@PreviewLightDark`               | Anything whose colours carry meaning — chips, badges, status pills, containers        |
| `@PreviewFontScale`               | Text-heavy rows and sections, where a large scale is what breaks the layout           |

They stack, and stacking is normal for a screen worth checking twice:

```kotlin
@PreviewScreenSizes
@PreviewFontScale
```

`widthDp`, `heightDp` and `showSystemUi` are for the case where **one specific breakpoint** is the
point (`@Preview(showBackground = true, widthDp = 360)` next to a `widthDp = 840`). Reaching for
them instead of `@PreviewScreenSizes` on a whole screen is a mistake.

## Preview the stateless half

A `…Page` resolves a ViewModel or a Circuit presenter and cannot render in a preview. Every screen
preview here calls the `…Content`, which takes plain values and lambdas:

```kotlin
@PreviewScreenSizes
@Composable
private fun TravelListPageSelectionModePreview() = KTravelTheme {
    TravelListContent(
        travelList = previewTravelList,
        isSelectionMode = true,
        selectedIds = persistentSetOf("1"),
        onTravelClick = {},
        newTravelClick = {},
    )
}
```

If a screen has no `…Content`, the preview is not the thing to work around: hoist the state first,
then preview the content. The same limit applies inside a screen — a block drawn through
`CircuitContent` cannot render, so the preview passes `null` for it and the KDoc on the fixture says
why (see `TransportPlanningPreviews.kt`).

Callbacks are empty lambdas: `{}`, or `{ _, _ -> }` for the arities that need it.

## Variants: one provider or several functions

Use a **`PreviewParameterProvider`** when the variants are values of the same shape — the states of
one component. One preview function, one provider, and the IDE draws the whole set:

```kotlin
/** What the pill knows about the navigator at one moment. */
internal data class ReachabilityPillPreviewState(
    val isChecking: Boolean,
    val isReachable: Boolean,
    val latencyMillis: Long?,
)

/** Checking, answered, answered without a timing, and silent. */
internal class ReachabilityPillPreviewParams : PreviewParameterProvider<ReachabilityPillPreviewState> {
    override val values = sequenceOf(
        ReachabilityPillPreviewState(isChecking = true, isReachable = false, latencyMillis = null),
        ReachabilityPillPreviewState(isChecking = false, isReachable = true, latencyMillis = 87),
        ReachabilityPillPreviewState(isChecking = false, isReachable = true, latencyMillis = null),
        ReachabilityPillPreviewState(isChecking = false, isReachable = false, latencyMillis = null),
    )
}
```

Naming, and both declarations `internal` in `…PreviewData.kt`:

| Declaration                     | Name                                      |
|---------------------------------|-------------------------------------------|
| The provider                    | `…PreviewParams`                          |
| The state it yields, when the composable takes more than one argument | `…PreviewState` |
| A plain fixture value           | `previewTravelList`, `PREVIEW_CATALOG`    |

Write a **second preview function** instead when the difference is structural rather than a value:
selection mode against normal mode, transit against road. Put the variant in the name.

When the composable takes a domain type directly, the provider yields that type — no wrapper state
class is invented for a single argument (`PreviewParameterProvider<NavigatorReachability>`).

## Which states the fixtures carry

A provider that lists the happy values is a provider that finds nothing. Include, whenever they
exist:

- the **empty** value — what the screen shows before the user has chosen anything;
- the **overflowing** value — a name longer than the row, against a `maxLines = 1` with no overflow
  set, so the cut is visible;
- the **error / unavailable / disabled** state, with its reason line;
- the **unknown** value — the profile from a newer server with no localized name and no icon, the
  enum entry this build has never heard of.

The provider's KDoc says *why those values*, not what they are. `TransportComponentPreviewData.kt`
is the reference: each block explains which state is the one likely to be laid out wrong.

## Sample data

- **Resource strings for anything the user reads**: `stringResource(Res.string.…)`, never a
  hardcoded label. Invented content (a place name, a trip name) may be free text.
- `LoremIpsum(words).values.first()` for filler prose.
- **Fixed dates and times** — `LocalDate(2026, 5, 18)`, `LocalTime(hour = 10, minute = 30)` — so the
  render is the same every time it is opened. A couple of older fixtures still call
  `Clock.System.now()`; they are not the model to copy.
- Immutable collections (`persistentListOf`, `persistentSetOf`): detekt's `Compose >
  UnstableCollections` is active, and it applies to fixtures like anything else.
- Coordinates and identifiers made up but well formed. No production data, no network, no database
  — a preview that needs either is a preview of the wrong composable.
- English identifiers and English KDoc, like the rest of the repository.

## What the tooling already handles

- **detekt** ignores `@Preview` and `@Composable` functions in `UnusedPrivateFunction`,
  `UnusedParameter`, `UnusedPrivateProperty` and `UnusedVariable`, so a private preview nobody calls
  is not a finding. `ModifierMissing` only checks public composables, so a preview needs no
  `modifier`. `PreviewNaming` is the rule that will fail the build: the suffix is not optional.
- **Kover** excludes `@Preview` functions by annotation and the `*PreviewsKt` / `*PreviewDataKt`
  classes by name. Fixtures anywhere else are counted.

## Verify

```bash
./gradlew :composeApp:compileKotlinJvm   # fast check that commonMain still compiles
./gradlew detektAll                      # PreviewNaming, UnstableCollections, formatting
```

Then open the preview pane on the Android target and look at every state the provider yields — a
preview that compiles and renders wrong has done nothing.

## Checklist

1. The preview is `private`, `@Composable`, named `…Preview`, with no `modifier` parameter.
2. Its body is `= KTravelTheme { … }`; a component preview adds `Surface { }` and padding.
3. The annotation matches the intent: component, screen sizes, light/dark, font scale.
4. It calls the stateless `…Content`, not a `…Page`.
5. Variants of the same shape go through a `…PreviewParams` provider; structural variants get their
   own function with the variant in the name.
6. The fixtures cover empty, overflowing, error and unknown — and their KDoc says why.
7. Providers and sample values live in `…PreviewData.kt`, previews of a screen in `…Previews.kt`,
   inline previews inside `//region Previews` / `//endregion Previews`.
8. Imports are `androidx.compose.ui.tooling.preview.*`; the file is in `commonMain`.
9. `./gradlew detektAll` is clean and the preview actually renders.

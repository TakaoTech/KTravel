---
name: previewing-composables
description: Write Compose Multiplatform previews the way KTravel writes them — every `@Preview` in a `//region Previews` block inside the file that declares the composable it draws, never in a sibling `…Previews.kt`, with `PreviewParameterProvider`s and sample values in a sibling `…PreviewData.kt` (that file name is what keeps fixtures out of the coverage report), the `androidx.compose.ui.tooling.preview` imports that come from the multiplatform `ui-tooling-preview` artifact, the `private fun …Preview() = KTravelTheme { … }` shape every preview takes, when to reach for `@Preview(showBackground = true)`, `@PreviewScreenSizes`, `@PreviewLightDark` or `@PreviewFontScale`, why a preview calls the stateless `…Content` and never the `…Page`, which states a provider has to carry, and what detekt and Kover already do with `@Preview`. Use whenever adding or changing a `@Preview`, adding a composable that deserves one, writing preview sample data, or when a preview will not render or trips detekt.
---

# Previewing KTravel composables

Every `@Preview` in this repository lives in `composeApp/src/commonMain` — never in `androidMain`,
`jvmMain` or `iosMain`. The renderer is the Android one
(`androidRuntimeClasspath(libs.compose.tooling)` in `composeApp/build.gradle.kts`), so a preview is
written once in shared code and drawn from the Android target of the IDE.

## When this applies

Add a preview when a composable **draws something**: a component with states, a section, a screen's
stateless content. Not to plumbing — a `…Page` that only wires a ViewModel, a `CompositionLocal`
provider, a `Modifier` extension.

A composable whose states the reader cannot guess (empty, error, disabled, truncated, unavailable)
needs one most: the preview is the only place those states are ever seen together.

## Where the preview goes

**A preview lives in the same file as the composable it draws**, at the bottom, inside a
`//region Previews` / `//endregion Previews` block. Component or screen, one preview or five: there
is no sibling `…Previews.kt`. Keeping the two together means a signature change and its preview are
one diff, instead of the preview rotting in a file nobody opened.

What moves out is the data. A literal written once stays inline (`name = "Piazza di Trevi"`); a
fixture goes to a sibling `…PreviewData.kt` in the same package as soon as it becomes a named
top-level declaration — a `PreviewParameterProvider`, a `val`, a builder function.

That split is what the coverage report is built on. The root
`kover { reports { filters { excludes { … } } } }` block excludes `@Preview`-annotated functions and
the `*PreviewDataKt` file facade. The annotation follows the function wherever it sits, so an inline
preview costs nothing; a top-level `val PREVIEW_CATALOG` or `fun previewSummary()` left in a
production file is neither annotated nor excluded and counts against coverage as uncovered
production code. A `PreviewParameterProvider` is a class of its own and matches neither pattern, so
it is counted wherever it sits — keep it in `…PreviewData.kt` anyway, which is where the next reader
looks for it and what makes a `classes("*PreviewParams")` filter possible later.

If the region starts to feel too long for the file, the composable has grown too big, not the
preview: split the composable, and each half takes its previews with it.

`preview` as a *package* name is reserved for Compose previews: shared providers live in
`ui/plan/preview/`, the user-facing route preview feature in `ui/plan/transport/routepreview/`. Do
not reintroduce the collision.

## Imports

`Preview`, `PreviewParameter`, `PreviewParameterProvider`, `PreviewLightDark`, `PreviewScreenSizes`
and `PreviewFontScale` come from `androidx.compose.ui.tooling.preview`, `LoremIpsum` from its
`.datasource` subpackage — AndroidX-shaped names supplied by the multiplatform `libs.compose.preview`
(`org.jetbrains.compose.ui:ui-tooling-preview`), already a `commonMain` `implementation` dependency,
so nothing has to be added to the build to write a preview. Never import
`org.jetbrains.compose.ui.tooling.preview.Preview`: no source here uses it and mixing the two splits
the annotation in half.

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

- **`private`**, always. The one public `@Preview` is `App()` in `App.kt` — the real entry point, not
  a preview of something else.
- **Name ends in `Preview`**, required by detekt's `Compose > PreviewNaming`: the composable plus the
  variant, as in `TravelListPageSelectionModePreview` or `PlaceStepContentEmptyNotePreview`.
- **Expression body assigned to `KTravelTheme { … }`.** The theme carries the brand colours, the type
  scale and the corner radii; without it a preview draws stock Material 3 and lies about the result.
- **No `modifier` parameter.** The only parameter a preview takes is a `@PreviewParameter`.
- **No KDoc.** `documenting-with-kdoc` exempts `@Preview` functions; the fixtures behind them are
  what gets documented.

Component previews wrap the component in `Surface { }` and pad it, so it is not drawn edge to edge on
a transparent ground. Screen previews call the content directly — the screen brings its own
`Scaffold` and background, and a second `Surface` only adds a layer.

## Choosing the annotation

| Annotation                        | Use it for                                                                       |
|-----------------------------------|----------------------------------------------------------------------------------|
| `@Preview(showBackground = true)` | A single component: a card, a pill, a row. The default, and the most common here |
| `@PreviewScreenSizes`             | A screen's `…Content`: the adaptive layouts are the thing being checked          |
| `@PreviewLightDark`               | Anything whose colours carry meaning — chips, badges, status pills, containers   |
| `@PreviewFontScale`               | Text-heavy rows and sections, where a large scale is what breaks the layout      |

They stack, and `@PreviewScreenSizes` over `@PreviewFontScale` is normal for a screen worth checking
twice. `widthDp`, `heightDp` and `showSystemUi` are for the case where **one specific breakpoint** is
the point (a `widthDp = 360` next to a `widthDp = 840`); reaching for them instead of
`@PreviewScreenSizes` on a whole screen is a mistake.

## Preview the stateless half

A `…Page` resolves a ViewModel or a Circuit presenter and cannot render in a preview. Every screen
preview here calls the `…Content`, which takes plain values and lambdas — callbacks are empty ones,
`{}` or `{ _, _ -> }` for the arities that need it:

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
`CircuitContent` cannot render, so the preview passes `null` for it and a comment above that argument
says why (`TransportPlanningContent` is the example).

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
        // …and the two the KDoc names: answered without a timing, silent.
    )
}
```

Naming, both declarations `internal` in `…PreviewData.kt`:

| Declaration                                                           | Name                                   |
|-----------------------------------------------------------------------|----------------------------------------|
| The provider                                                          | `…PreviewParams`                       |
| The state it yields, when the composable takes more than one argument | `…PreviewState`                        |
| A plain fixture value                                                 | `previewTravelList`, `PREVIEW_CATALOG` |

Write a **second preview function** instead when the difference is structural rather than a value:
selection mode against normal mode, transit against road, with the variant in the name. When the
composable takes a domain type directly the provider yields that type — no wrapper state class is
invented for a single argument (`PreviewParameterProvider<NavigatorReachability>`).

## Which states the fixtures carry

A provider that lists the happy values is a provider that finds nothing. Include, whenever they
exist:

- the **empty** value — what the screen shows before the user has chosen anything;
- the **overflowing** value — a name longer than the row, against a `maxLines = 1` with no overflow
  set, so the cut is visible;
- the **error / unavailable / disabled** state, with its reason line;
- the **unknown** value — the profile from a newer server with no localized name and no icon, the
  enum entry this build has never heard of.

The provider's KDoc says *why those values*, not what they are; `TransportComponentPreviewData.kt` is
the reference.

## Sample data

- **Resource strings for anything the user reads**: `stringResource(Res.string.…)`, never a hardcoded
  label. Invented content (a place name, a trip name) may be free text.
- `LoremIpsum(words).values.first()` for filler prose.
- **Fixed dates and times** — `LocalDate(2026, 5, 18)`, `LocalTime(hour = 10, minute = 30)` — so the
  render is the same every time it is opened. Older fixtures calling `Clock.System.now()` are not the
  model to copy.
- Immutable collections (`persistentListOf`, `persistentSetOf`): detekt's `Compose >
  UnstableCollections` applies to fixtures like anything else.
- Made-up but well-formed coordinates and identifiers. No production data, no network, no database —
  a preview that needs either is a preview of the wrong composable.
- English identifiers and English KDoc, like the rest of the repository.

## What the tooling already handles

- **detekt** ignores `@Preview` and `@Composable` functions in `UnusedPrivateFunction`,
  `UnusedParameter`, `UnusedPrivateProperty` and `UnusedVariable`, so a private preview nobody calls
  is not a finding, and `ModifierMissing` only checks public composables. `PreviewNaming` is the rule
  that will fail the build: the suffix is not optional.
- **Kover** excludes `@Preview` functions by annotation — which is what makes an inline preview free
  — and the `*PreviewDataKt` class by name. Fixtures anywhere else are counted.

## Verify

```bash
./gradlew :composeApp:compileKotlinJvm   # fast check that commonMain still compiles
./gradlew detektAll                      # PreviewNaming, UnstableCollections, formatting
```

Then open the preview pane on the Android target and look at every state the provider yields — a
preview that compiles and renders wrong has done nothing.

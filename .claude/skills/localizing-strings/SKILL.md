---
name: localizing-strings
description: Add, translate, or audit user-facing strings in KTravel's Compose Multiplatform resources. Encodes the rule that `composeResources/values/strings.xml` holds English only and every translation lives in a language-qualified folder such as `values-it/strings.xml`, with identical keys, ordering, and section grouping across all files. Use when adding a new label, `stringResource`, `pluralStringResource`, or `contentDescription`; when extracting a hardcoded literal out of a composable; when adding a new language; or when the user mentions strings.xml, values-it, localization, translations, or "why is this label in Italian".
---

# Localizing strings in KTravel

## The rule

- `composeApp/src/commonMain/composeResources/values/strings.xml` is the **default** resource file
  and contains **English only**. A non-English literal in this file is a bug.
- Translations live in language-qualified folders next to it, one file per language:
    - `values-it/strings.xml` — Italian
- Every key in `values/strings.xml` must exist in **every** localized file. Keep key order and the
  blank-line section grouping identical across files so they diff cleanly.
- No user-facing literal belongs in a composable. Always go through `stringResource` /
  `pluralStringResource`.

## Adding a string

1. Add the key to `values/strings.xml` in the section that matches its feature (`place_insert_*`,
   `planning_detail_*`, `planning_trip_*`, `travel_creation_*`, `travel_selection_*`, …), with the *
   *English** text.
2. Add the same key, in the same position, to `values-it/strings.xml` with the Italian text.
3. Reference it from Compose:

```kotlin
Text(stringResource(Res.string.planning_trip_add_place))
Text(pluralStringResource(Res.plurals.travel_selection_selected_count, count, count))
```

Naming: `<feature>_<element>` for visible labels, `<feature>_cd_<element>` for content descriptions,
`<feature>_error_<case>` for validation messages.

## Formatting notes

- Positional placeholders: `%1$s`, `%1$d` — keep the same indices in every translation.
- `\n` is the escape for a newline inside a string value.
- Plain apostrophes (`l'itinerario`) are written unescaped in this project — do not add `\'`.
- Em dash `—` and accented characters are written literally; the files are UTF-8.

## Verifying alignment

```bash
./gradlew :composeApp:checkStringResourceParity
```

The task is registered in `composeApp/build.gradle.kts` and `check` depends on it. It takes
`values/strings.xml` as the reference and fails when a translation is missing one of its keys,
declares a key the default file does not have, declares the same key twice, or uses different
positional placeholders; a key order that diverges from the reference is reported as a warning. The
report is written to `composeApp/build/reports/string-resource-parity.txt`.

Nothing else in the toolchain catches this. Compose Resources merges every qualifier into one
accessor set, so a key that exists only in `values-it` still generates `Res.string.<key>` and the
build succeeds — the failure is a runtime `Resource with ID='...' not found` on the first device
whose locale has no match. Android Lint's `MissingTranslation` only reads `res/` folders, and
`composeResources` is converted to CVR and copied into the assets.

## Adding a new language

Create `composeResources/values-<lang>/strings.xml` (e.g. `values-es`), copy `values/strings.xml`
verbatim, translate every value, and run the verification above. Compose Resources picks the folder
up automatically — no Gradle change is needed.

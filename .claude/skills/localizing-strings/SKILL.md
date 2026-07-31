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

After touching any resource file, check that all languages carry the same keys in the same order:

```bash
cd composeApp/src/commonMain/composeResources
for f in values/strings.xml values-it/strings.xml; do
  python3 -c "
import xml.etree.ElementTree as ET
r = ET.parse('$f').getroot()
print('\n'.join(f'{e.tag} {e.get(\"name\")}' for e in r))
" > "/tmp/keys_$(echo "$f" | tr / _)"
done
diff /tmp/keys_values_strings.xml /tmp/keys_values-it_strings.xml && echo "keys aligned"
```

This also fails loudly if either file is malformed XML.

## Adding a new language

Create `composeResources/values-<lang>/strings.xml` (e.g. `values-es`), copy `values/strings.xml`
verbatim, translate every value, and run the verification above. Compose Resources picks the folder
up automatically — no Gradle change is needed.

---
name: documenting-with-kdoc
description: Write KDoc for KTravel's Kotlin code following the two upstream standards this project defers to — the JetBrains Kotlin coding conventions (documentation comments) and the AndroidX KDoc guidelines — including how to resolve the one place where they disagree (JetBrains says avoid `@param` / `@return` when the prose can carry them, AndroidX says document every parameter and property), the shared syntax rules (Markdown not HTML, `[links]` not `{@link}`, backticked code, `@param T` not `@param <T>`, `@Deprecated` annotation not `@deprecated`, qualified `@see`, no self-links), the single vs multi-line comment form, and what detekt's `UndocumentedPublicClass` / `UndocumentedPublicFunction` / `UndocumentedPublicProperty` rules require. Use whenever adding or changing a public class, interface, object, function, property, sealed hierarchy or composable; when a signature changes and its KDoc no longer matches; when detekt reports an undocumented public declaration; or when the user asks for documentation, KDoc, or docs on some code.
---

# Documenting KTravel code with KDoc

## The standards this project follows

Two upstream documents are normative here. Follow them; the sections below only resolve where they
disagree and record what this repository adds.

| Source                                                                                                                  | Authority over                                                    |
|-------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|
| [Kotlin coding conventions — Documentation comments](https://kotlinlang.org/docs/coding-conventions.html#documentation-comments) | Comment form, voice, and the preference for prose over tags        |
| [KDoc syntax reference](https://kotlinlang.org/docs/kotlin-doc.html)                                                     | The tag set and the inline markup syntax                          |
| [AndroidX KDoc guidelines](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/docs/kdoc_guidelines.md) | Coverage of public API surface, `@sample`, `@see`, linking rules   |

## When this applies

Document **while writing the code**, in the same edit — detekt fails the build otherwise.

`config/detekt/detekt.yml` (`comments` section) has `UndocumentedPublicClass`,
`UndocumentedPublicFunction` and `UndocumentedPublicProperty` active, so every `public` class,
interface, object, enum, annotation, function and property needs a KDoc block. All three exclude
the test source sets (`**/commonTest/**`, `**/jvmTest/**`, `**/androidTest/**`, `**/iosTest/**`),
so **tests are not documented**: the `Given … When … Then …` name is the documentation.
`internal` and `private` declarations are not required to carry KDoc, but document them when the
reason they exist is not obvious.

`OutdatedDocumentation` is off, so nothing catches a stale `@param` after a rename. Changing a
signature means changing its KDoc in the same edit.

Verify with `./gradlew detektAll`.

## Form

Per the Kotlin coding conventions:

```kotlin
/** A short documentation comment fits on one line. */

/**
 * A longer one puts the opening `/**` on its own line and starts every
 * following line with an asterisk.
 */
```

- The first paragraph is the summary; a blank `*` line separates it from the body, and another
  separates the body from the block tags, which come last.
- Third person, present tense, as the Kotlin documentation itself writes it: *"Returns the absolute
  value of the given [number]."* Never "This method returns…", never an imperative.
- Wrap at **100 columns**, comment markers included — the Google/AndroidX column limit, and what
  every documented file here already does. detekt's `MaxLineLength` excludes comments, so nothing
  enforces it for you.
- English only, like every artefact in this repository, including when the conversation with the
  user is in Italian.

## `@param` and `@return`: the one place the standards disagree

JetBrains says to **avoid** `@param` and `@return` and to fold the description into the prose,
linking the parameter, reserving the tags for descriptions too long to sit in the flow:

```kotlin
/** Returns the absolute value of the given [number]. */
fun abs(number: Int): Int
```

AndroidX says **every parameter and property is documented** with an explicit `@param` /
`@property`, because that is what generates the entries in the reference documentation, and to
cover generic types, receivers, return types and constructors too.

Resolve it by audience:

| Code                                                                          | Rule                                                                                                                                  |
|---------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `:gunzou-api`, `:gunzou-client`, `:password-strength` — API surface others consume | **AndroidX**: an explicit `@param` / `@property` for every parameter, property and type parameter; `@return` and `@throws` where they apply |
| `:composeApp`, `:gunzou-server`, `:gunzou-here-client` — application code       | **JetBrains**: prose with `[links]`, tags only where a parameter needs more than the summary can carry                                    |

Either way, a declaration is documented consistently: an argument list with one `@param` filled in
and four missing reads as an oversight, so document all of them or fold all of them into the prose.

## Tags

| Tag                         | Rule                                                                                            |
|-----------------------------|-------------------------------------------------------------------------------------------------|
| `@param name`               | Spelling matches the declaration **exactly**. Type parameters are `@param T`, never `@param <T>` |
| `@property name`            | For constructor properties and interface properties — a `val` in the primary constructor is a property, not a `@param` |
| `@return`                   | Only when the return needs more than the summary said                                            |
| `@throws Type` / `@exception` | For every exception a caller is expected to handle                                             |
| `@constructor`              | When the constructor does something the class summary does not cover                             |
| `@receiver`                 | For an extension whose receiver carries a contract of its own                                    |
| `@see Foo.bar`              | Always qualified — `@see MyClass.myFun`, never `@see .myFun`. Never mixed with a Markdown link in the same statement |
| `@sample fully.qualified.fn`| AndroidX asks for one on a standalone or advanced feature. Nothing in this repository compiles sample sources yet, so introduce one only together with the source set that holds it |
| `@suppress`                 | Hides the declaration from generated documentation                                               |

KDoc has **no `@deprecated`**: use the `@Deprecated` annotation with `ReplaceWith`.

## Inline markup

- Markdown, never HTML — no `<p>`, no `<code>`.
- Links are `[Foo]`, `[Foo.bar]`, `[custom label][Foo]`, fully qualified when the target is not
  imported. Never `{@link}`.
- Code, literals and values go in backticks: `` `true` ``, `` `null` ``, `` `.ktravel` ``.
- **Do not link the declaration being documented.** Documenting `Item`, write "Item is …", not
  "[Item] is …".

## What a good summary says

Say what the declaration is for. Restating the signature adds nothing: `fun labelOrNull():
StringResource?` does not need "returns a nullable `StringResource`", it needs "The localized name
of a profile, or null when this build has never heard of it."

Where the reason for a declaration's shape is not obvious, the body is the place to record it —
why it is separate, what breaks if a reader folds it into something else. If there is nothing
non-obvious to say, stop after the summary; padding is worse than brevity.

## What not to document

- Test sources — the test name carries it.
- `override` members whose supertype documentation still applies; document the override only when
  it changes the contract.
- Generated code, `@Preview` functions, `TestTags` objects, DI plumbing.
- Restatements of the type: "The list of places" over `places: List<PlaceDomain>`.

## Compose

Neither standard covers composables (the AndroidX Compose API guidelines are about naming and API
shape, not documentation), so apply the general rules with these additions:

- Document the composable as a piece of the screen — what it draws and where it sits, not how it
  recomposes.
- Document **slots** (`content: @Composable () -> Unit`) and callbacks: a slot's contract is
  invisible in its type, so say what the caller is expected to put there.
- State what a default means when it is not obvious from the value.
- `modifier: Modifier = Modifier` is conventional and needs no entry.

## Multiplatform

- The `expect` declaration carries the documentation: it is the contract every platform honours.
- An `actual` is documented only where the platform changes what a caller must know — a permission
  it needs, a path it writes to, a target where it is a no-op. Link back to the `expect` instead of
  copying its prose.
- In `:gunzou-api` the KDoc *is* the wire contract's specification: document units, coordinate
  order, what `null` means and what an empty collection means, because server and client are read
  by different people.

## Checklist

1. Every new or changed public class, function and property has KDoc.
2. Summary in third person, present tense, no signature restatement.
3. Tags follow the module's rule from the table above, and none is left over from an older signature.
4. `@param` names match exactly; no `<T>`; `@see` qualified; no self-link.
5. Markdown only, code in backticks, `@Deprecated` instead of `@deprecated`.
6. Wrapped at 100 columns, in English.
7. `./gradlew detektAll` is clean.

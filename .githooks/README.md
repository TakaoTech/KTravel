# Git hooks

These hooks run locally the same Gradle commands `.github/workflows/ci.main.kts` runs on CI, so a
push does not fail on something a local run would have caught.

| Hook         | CI job   | Command                                                                      | Runs when                             |
|--------------|----------|------------------------------------------------------------------------------|---------------------------------------|
| `pre-commit` | `format` | `./gradlew detektFormat -Pdetekt.autocorrect=true`, then re-stages the result | a `.kt` / `.kts` file is staged       |
| `pre-commit` | `verify` | `./gradlew :composeApp:checkStringResourceParity`                            | a `composeResources` `strings.xml` is staged |

## Install

```bash
git config core.hooksPath .githooks
```

The hooks are versioned, so this is a one-off per clone; updates arrive with the next `git pull`.
To uninstall: `git config --unset core.hooksPath`.

## Escape hatches

- `git commit --no-verify` — skip the hook for one command.
- `KTRAVEL_SKIP_HOOKS=1` — skip hook

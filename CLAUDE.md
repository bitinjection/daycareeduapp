# CLAUDE.md

@README.md

The README is the source of truth for prerequisites, commands, architecture
rules, and the build-order checklist. Keep it current rather than duplicating
it here. This file holds only what matters when working in the repo as an agent.

## Where we are

Stages 0–2 are done. **Next is stage 3**: Expo + shadow-cljs `:react-native`
target, "Hello" on a real tablet. Do it alone — no other work in flight — so
any breakage is attributable to the shadow-cljs ↔ Metro ↔ Expo seam. If it
fights us, the `:browser` target is the fallback.

Open to-do: make the CI `verify` job a required status check on `main`
(GitHub branch protection — a settings change, not a code change).

When a stage completes, tick it in the README checklist and update this section.

## Working rules

- **Run `npm run verify` before calling anything done.** It is exactly what CI
  runs (lint, format check, tests).
- **Test-first for `daycare.activities.*`.** Write the failing test, watch it
  fail, then implement. Prefer properties (test.check) over examples where an
  invariant exists, e.g. "raising difficulty never yields an easier round".
- **A gate isn't trusted until it's been seen to fail.** When adding a check,
  break something deliberately and confirm it goes red.
- **Keep pure logic pure.** No React, storage, device APIs, or `Math/random`
  in `daycare.activities.*`. Randomness comes from an explicit seed.
- **Pedagogy is a design constraint, not decoration.** E.g. distractors are
  drawn near the target — offering 7 and 8 teaches counting; 7 and 19 teaches
  guessing. Preserve the reasoning when changing such code.
- **Never edit or commit `android/` or `ios/`.** Native config goes in
  `app.config.js` / Expo config plugins.
- **Before adding a dependency**, check it works on Fire OS (no Google Play
  Services) and contains no third-party analytics or ad SDK (COPPA, Apple Kids
  Category). Prefer `expo-*` packages.
- Dependencies go in `shadow-cljs.edn`, not `deps.edn` or `package.json`
  (npm packages for the RN app excepted).

## Commits

Trunk-based on `main`, guarded by CI. Commit messages explain *why* (see
`git log` for the house style): a short subject, then grouped bullets with the
reasoning behind non-obvious choices. Don't commit or push unless asked.

## Environment gotchas

- Windows dev machine. Writing files from PowerShell with
  `Set-Content -Encoding utf8` adds a BOM, which the CLJS compiler reports as
  `Use of undeclared Var cljs.user/?` at line 1. Use the Write/Edit tools, or
  `npm run fmt:fix` to strip it.
- JDK 21 is required; Java 20 fails with `UnsupportedClassVersionError`.
- `npm run fmt:*` goes through `scripts/run-clj.mjs`, which trims the JVM
  stack trace — the real error message is at the top of the output.

# CLAUDE.md

@README.md

The README is the source of truth for prerequisites, commands, architecture
rules, and the build-order checklist. Keep it current rather than duplicating
it here. This file holds only what matters when working in the repo as an agent.

## What we're building

An educational app for our home daycare, written with an eye to eventual
release on the Play Store and App Store. It is a suite of **teacher-guided**
activities, with two views running on separate devices:

- **Student view** — the daycare-aged kids interact with the activities.
- **Teacher view** — a teacher on another device remotely controls the student
  view, e.g. advancing to the next activity or page.

Specific activities will be defined later; don't invent them. Written in
ClojureScript with the usual toolchain for this kind of app (see the README).

### Teacher–student connection

- **Goes through a server, not the local network.** The two devices may not be
  on the same Wi-Fi, and users shouldn't have to think about networking.
- **The server is written in Clojure or ClojureScript.**
- **Deferred.** Don't start server work until a boilerplate student/teacher app
  runs successfully in a simulator and on a real device.
- **COPPA is noted, to be addressed later.** Sending data through a server makes
  it relevant, though we expect to collect minimal student information, if any.

## Where we are

Stages 0–5 are done: ClojureScript renders through Expo SDK 58 on the
`Tablet_API_30` emulator, and a save hot-reloads it. **Next is stage 6**: a
development build on a real SGIN tablet, then an advanced-compiled release build.
The first major task is Phase 1 of the README's build order, which gets the
toolchain and development loop working through to a boilerplate student/teacher
app on an Android emulator and a real Android tablet.

Current scope:

- **Android first, developed on Windows.** Keep the App Store in mind, but don't
  do iOS work yet. There is no Mac available.
- **Test devices.** The target is low-spec SGIN kids tablets, advertised as
  Android 12 or 13. Those specs are unverified, so check them over `adb`. A
  Samsung S21 phone is the fallback if the tablets can't run the app. iPads are
  available for later. Performance on the SGIN tablets matters, so judge it
  there and in release builds, not on the emulator.
- **Fire tablets stay a possible target**, though none is available for testing,
  so keep avoiding anything that needs Google Play Services.
- **One app for both roles.** The teacher and student views are the same app.
  The server will be a separate project.
- **The repo is public**: <https://github.com/bitinjection/daycareeduapp>. Never
  commit child names or photos, home addresses, keys, or signing credentials.
- **CI comes at stage 10.** GitHub Actions is turned off for the repo, and the
  workflow file has never run, so don't treat it as a gate before then. Until
  then, the gates are `npm run verify` and the lefthook hooks.

To start stage 6, the owner connects an SGIN tablet with USB debugging on. Read
its real Android version and memory over `adb` before relying on it.

The Android package ID in `app.config.js` is a placeholder. Choose the real one
before the first store release, because the Play Store treats it as permanent.

Assumptions to prove in Phase 1, not yet tested together:

- **Advanced compilation.** Only development builds have run. Stage 6 tests the
  release build, where missing externs would first show up.
- **State surviving a hot reload.** After one save, Metro also re-bundled
  `app/index.js` and shadow-cljs reported a new "ready", so both may reload the
  code. There is no app state yet to show the effect. Test this in stage 7: change
  re-frame state, save a file, and confirm the state is still there.

- **Reagent 2.0.1 with React Native.** re-frame 1.4.7 declares Reagent 1.2.0,
  while React 19 needs Reagent 2.x, so pin 2.0.1 explicitly. Its source calls out
  `react-dom/flushSync`, and React Native has no `react-dom`.
- **Fire OS.** Fire OS 8 is API 30, and Expo SDK 58 targets API 36. This stays
  unproven through Phase 1.

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

Trunk-based development on `main`, at least to begin with: no long-lived feature
branches. Commit messages explain *why* (see
`git log` for the house style): a short subject, then grouped bullets with the
reasoning behind non-obvious choices. Don't commit or push unless asked.

## Environment gotchas

- **Don't set `CI=1` when starting Metro for development.** With it set, an edit
  never reached the app; without it, the same edit hot-reloaded in about 2s.
- **Drive C is nearly full, and it's the only drive.** Check free space before
  large installs or builds. A first native build took about 7.5 minutes and used
  several GB.
- **Stopping a background `expo start` can leave Metro running.** Check that port
  8081 is free before restarting it.
- The `Tablet_API_30` emulator is API 30, 64-bit, with no Google services, so it
  would expose a hidden Play Services dependency.
- Windows dev machine. Writing files from PowerShell with
  `Set-Content -Encoding utf8` adds a BOM, which the CLJS compiler reports as
  `Use of undeclared Var cljs.user/?` at line 1. Use the Write/Edit tools, or
  `npm run fmt:fix` to strip it.
- JDK 21 is required; Java 20 fails with `UnsupportedClassVersionError`.
- `npm run fmt:*` goes through `scripts/run-clj.mjs`, which trims the JVM
  stack trace — the real error message is at the top of the output.

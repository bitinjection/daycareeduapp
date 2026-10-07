# daycareeduapp

Educational activities for tablets, built in ClojureScript.

## Prerequisites

| | Version | Note |
|---|---|---|
| JDK | **21+** | shadow-cljs bundles a Closure Compiler built for Java 21. Java 20 fails with `UnsupportedClassVersionError`. |
| Node | 22 LTS | |

No Clojure CLI needed — dependencies are declared in `shadow-cljs.edn` and
formatting runs through shadow-cljs's own JVM.

```
npm install
npx lefthook install     # one time, installs the git hooks
```

## Commands

```
npm run test:watch   # the dev loop: recompiles and reruns tests on save
npm test             # one-shot, reliable exit code (what CI runs)
npm run lint         # clj-kondo
npm run fmt:check    # cljfmt, read-only
npm run fmt:fix      # cljfmt, rewrites files
npm run verify       # everything CI runs, locally
```

## Layout

```
src/main/   production code
src/test/   tests, mirroring src/main
dev/        tooling that runs on the JVM, not in the app
scripts/    node helpers for npm scripts
```

Note for Windows: `npm run fmt:check` also rejects a UTF-8 byte-order mark.
PowerShell's `Set-Content -Encoding utf8` writes one, and the ClojureScript
compiler reads it as a stray symbol, reporting a confusing
`Use of undeclared Var cljs.user/?` at line 1. `npm run fmt:fix` strips it.

## Architecture rules

These exist because they are cheap now and expensive to retrofit.

**Three layers, and only the first one is tested.** Pure activity logic
(`daycare.activities.*`) has no React, no storage, and no device APIs — it is a
function of its arguments, and it is where test-driven development lives.
Above it sit re-frame events (testable headlessly) and then views, which stay
dumb enough not to need tests. Testing React Native views from ClojureScript is
unpleasant; the answer is to make views trivial rather than to fight it.

**Seeded, not random.** Activity generators take an explicit seed. A failing
property test then reports a seed that reproduces the exact round.

**The native projects are generated, never committed.** `android/` and `ios/`
come from `expo prebuild` and are gitignored. Native configuration belongs in
`app.config.js` and Expo config plugins. Hand-editing Gradle or Xcode files
creates a platform we maintain by hand.

**Stay inside the `expo-*` package set.** It is the capability surface that
works identically on both platforms. In particular, avoid anything requiring
Google Play Services (Firebase Cloud Messaging, Google Sign-In, Play Billing) —
Fire OS tablets do not have it.

**No third-party analytics SDKs.** An app directed at under-13s falls under
COPPA, and Apple's Kids Category prohibits third-party analytics and ad SDKs
outright. Telemetry must be aggregate and non-identifying, with no child names.

**Release flags are temporary; entitlement flags are not.** They live in
separate namespaces so release flags actually get deleted. A test fails the
build when a release flag outlives its expiry date.

## Build order

Staged so that the one genuinely risky integration — the
shadow-cljs ↔ Metro ↔ Expo seam — happens alone, with nothing else in flight.

- [x] **0** — git, JDK, Node
- [x] **1** — shadow-cljs + `:node-test`, first activity logic written test-first
- [x] **2** — clj-kondo, cljfmt, lefthook, GitHub Actions
- [ ] **3** — Expo + `:react-native` target, "Hello" on a real tablet ← *the risky one*
- [ ] **4** — Malli content-pack schemas
- [ ] **5** — first real screen
- [ ] **6** — persistence (expo-sqlite, UUIDv7, timestamps)
- [ ] **7** — flag namespaces, grown-up gate

If stage 3 fights us, nothing downstream is built on it yet: the test loop and
CI still work, and the `:browser` target is a fallback.

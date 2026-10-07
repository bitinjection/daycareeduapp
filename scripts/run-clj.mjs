// Runs `shadow-cljs clj-run <fn>` and trims the stack trace.
//
// clj-run reports a thrown exception with the full JVM trace, which buries the
// actual message under ~50 frames of shadow-cljs internals. Our dev tasks throw
// deliberately to signal failure, so the trace is always noise. Exit code is
// passed through unchanged.
//
//   node scripts/run-clj.mjs daycare.dev.format/check

import { spawnSync } from "node:child_process";

const [fn, ...args] = process.argv.slice(2);

if (!fn) {
  console.error("usage: node scripts/run-clj.mjs <fully/qualified-fn> [args...]");
  process.exit(2);
}

const result = spawnSync("npx", ["shadow-cljs", "clj-run", fn, ...args], {
  encoding: "utf8",
  // Needed on Windows, where npx is a shell script rather than an executable.
  shell: true,
});

const output = (result.stdout ?? "") + (result.stderr ?? "");
const traceStart = output.indexOf("failed to run function:");

process.stdout.write(traceStart === -1 ? output : output.slice(0, traceStart));
process.exit(result.status ?? 1);

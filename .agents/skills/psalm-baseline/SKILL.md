---
name: psalm-baseline
description: Run Psalm static analysis on a PHP project, interpret its baseline file, and fix issues instead of growing the baseline — per Equisoft AWT ASR-08/ASR-10.
---

# Psalm Baseline Management

Use this skill when asked to run Psalm on a PHP project, triage Psalm errors, or work with an
existing `psalm.baseline.xml`.

## Running Psalm

- Single file:
  ```shell
  ./vendor/bin/psalm --no-progress --output-format=text --no-diff path/to/File.php
  ```
- Whole codebase:
  ```shell
  ./vendor/bin/psalm --no-progress --output-format=text --no-diff
  ```
- `errorLevel` in `psalm.xml` controls strictness (1 = strictest). Do not lower it to silence new
  findings — that weakens code-quality analysis (ASR-08) for the whole project, not just the
  file you're touching.

## The baseline file

- `psalm.baseline.xml` is a list of **pre-existing** errors Psalm ignores so CI doesn't fail on
  legacy debt it didn't introduce. It is not a place to file new violations.
- **Never add new entries to grow the baseline.** If your change introduces a new Psalm error,
  fix the error — do not suppress it via the baseline.
- If your change happens to fix a pre-existing error that's listed in the baseline, **remove
  that baseline entry** in the same PR; leaving stale entries around blocks `findUnusedBaselineEntry`
  cleanup and hides the fact the error is gone.
- Regenerating the whole baseline (`--set-baseline`) is a last resort for a dedicated cleanup PR,
  not something to run routinely alongside feature work.

## Taint analysis (SAST)

- Psalm's taint-analysis mode is the project's SAST tool per ASR-10. Findings are exported as
  SARIF and uploaded to GitHub Advanced Security (GHAS).
- Critical/high-severity taint findings block merge — treat them the same as any other blocking
  SAST result, not as a Psalm-specific quirk to route around.

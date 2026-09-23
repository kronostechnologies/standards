---
description: Review the current pull request diff for ASR compliance and summarize findings.
---

# Review PR for ASR Compliance

Review the changes in this pull request against the Equisoft AWT organization's ASR
standards (`org-standards` instructions package) and any applicable ecosystem-specific
instructions (`kotlin-backend`, `frontend`, `php-backend`).

1. Identify the languages/ecosystems touched by the diff.
2. Check testing, security, dependency management, and repository-convention rules relevant to
   those ecosystems (see the `asr-reviewer` agent for the full checklist).
3. Summarize findings as a short markdown list grouped by severity (blocking / non-blocking).
4. If the diff is compliant, state that explicitly instead of listing empty sections.

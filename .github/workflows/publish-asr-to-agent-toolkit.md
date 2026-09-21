---
name: Publish ASR Instructions to agent-toolkit

on:
  push:
    branches:
      - master
    paths:
      - "docs/instructions/asr.instructions.md"
      - ".github/workflows/publish-asr-to-agent-toolkit.md"
  workflow_dispatch:

permissions:
  contents: read
  copilot-requests: write

network: defaults

safe-outputs:
  create-pull-request:
    target-repo: "kronostechnologies/agent-toolkit"
    base-branch: "main"
    title-prefix: "QCTECH-5463 "
    labels: [automation, apm]

---

You are publishing the org-wide ASR instructions into the `agent-toolkit` APM mono-repo, which
mirrors this file as the `org-standards` package so it can be consumed via APM
(`kronostechnologies/agent-toolkit/plugins/org-standards`).

## Instructions

1. Read `docs/instructions/asr.instructions.md` from this repository (`kronostechnologies/standards`,
   `master` branch) — this is the freshly generated source of truth.
2. In the target repository (`kronostechnologies/agent-toolkit`), read
   `plugins/org-standards/.apm/instructions/asr.instructions.md` and `plugins/org-standards/apm.yml`.
3. Replace the body of `plugins/org-standards/.apm/instructions/asr.instructions.md` with the
   content read in step 1, **but preserve the existing YAML frontmatter fence** (the
   `---\ndescription: ...\napplyTo: "**"\n---` block) at the top of the file — do not let the
   generated content overwrite or duplicate it.
4. If the body content actually changed (ignoring the frontmatter), bump the `version` field in
   `plugins/org-standards/apm.yml` following SemVer: PATCH for wording/formatting-only changes,
   MINOR for added/removed rules, MAJOR only if a rule is fully reversed (rare). If the content is
   unchanged, make no edits and do not open a pull request.
5. Open a pull request in `kronostechnologies/agent-toolkit` with these two file changes, titled
   `sync: update org-standards from kronostechnologies/standards@<short-sha>` and a description
   that links back to the commit in `kronostechnologies/standards` that triggered this run.

Do not modify anything else in `agent-toolkit` — not other packages, not the root `apm.yml`, not
`apm.lock.yaml`.

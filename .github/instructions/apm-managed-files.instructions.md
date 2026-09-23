---
description: Guardrail for files deployed by APM — do not hand-edit them.
applyTo: ".github/instructions/**,.github/prompts/**,.github/agents/**,.claude/**,.cursor/**,.agents/skills/**,.github/mcp.json,apm.yml,apm.lock.yaml"
---

# APM-Managed Files

The files matched by this instruction are deployed by [APM](https://microsoft.github.io/apm/) from
[`kronostechnologies/agent-toolkit`](https://github.com/kronostechnologies/agent-toolkit) (and, for
the `org-standards` package, ultimately generated from ASRs in `kronostechnologies/standards`).

**Do not hand-edit them.** `apm.lock.yaml` pins a content hash for every deployed file; `apm audit`
verifies deployed files against those hashes and CI fails on drift; the next `apm install` silently
overwrites any local edit anyway. Neither failure mode is a substitute for actually changing the
content.

To change one of these files: edit the source package in `agent-toolkit` (or the ASRs in
`standards` for `org-standards` content), then run `apm install` here to pick up the update. To
change *which*
packages or versions are installed, edit this repository's `apm.yml`, then run `apm install` and
commit the resulting `apm.lock.yaml` and deployed files together.

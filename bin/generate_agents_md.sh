#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

PROMPT_FILE="$ROOT_DIR/prompts/generate-agents-md.md"
PROMPT="$(cat "$PROMPT_FILE")"

gh copilot \
    -C "$ROOT_DIR" \
    -p "$PROMPT" \
    --model claude-sonnet-4.6 \
    --allow-all-tools \
    --no-custom-instructions \
    --no-ask-user \
    -s

echo "Successfully generated AGENTS.md"

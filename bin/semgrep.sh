#!/usr/bin/env bash

# This file is read by external tools. Do not move.
# Usage example in external projects:
#  curl -sSL https://raw.githubusercontent.com/kronostechnologies/standards/master/bin/semgrep.sh | bash -s -- -h

set -eo pipefail

DEFAULT_OUTPUT_FILE="./semgrep.sarif"
DEFAULT_TARGET_DIR=$(pwd)

usage() {
  echo "Usage: $0 -l <kt|ts|py> [-o result-file] [dir-to-scan]"
}

while getopts ":o:l:h" option; do
    case $option in
        o)
            output_file=${OPTARG}
            ;;
        l)
            language=${OPTARG}
            [[ $language == "kt" || $language == "ts" || $language == "py" ]] || usage
            ;;
        h)
            usage
            exit 0
            ;;
        *)
            usage
            exit 1
            ;;
    esac
done

if [[ -z "${language}" ]]; then
    usage
    exit 1
fi

output_file=${output_file:-$DEFAULT_OUTPUT_FILE}

shift $(($OPTIND - 1))
target_dir=${1:-$DEFAULT_TARGET_DIR}

lang_config=""
case $language in
    kt)
        lang_config="
            --config=p/kotlin \
            --exclude=\"*.kts\" \
        "
        ;;
    ts)
        lang_config="
            --strict \
            --config=p/typescript \
            --config=p/react \
        "
        ;;
    py)
        lang_config="
            --strict \
            --config=p/python \
            --config=p/gitlab-bandit \
        "
        ;;
esac

docker run --rm -v "$target_dir:/src" --user "$(id -u):$(id -g)" \
    returntocorp/semgrep:0.80.0 \
    --error \
    --sarif \
    --output=$output_file \
    --config=p/ci \
    --config=p/security-audit \
    $lang_config \
    .

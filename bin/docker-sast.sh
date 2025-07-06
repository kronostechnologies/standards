#!/usr/bin/env bash

# This file is read by external tools. Do not move.
# Usage example in external projects:
#  curl -sSL https://raw.githubusercontent.com/kronostechnologies/standards/master/bin/docker-sast.sh | bash -s -- -h

set -eo pipefail

DEFAULT_OUTPUT_FILE="./docker-sast.sarif"
# renovate: datasource=docker depName=aquasec/trivy
TRIVY_VERSION="0.64.1"

usage() {
  echo "Usage: $0 [-o result-file] [image-to-scan]"
}

while getopts ":o:h" option; do
  case $option in
  o)
    output_file=${OPTARG}
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

shift $(($OPTIND - 1))
if [ $# -ne 1 ]; then
  usage
  exit 1
fi

output_file=${output_file:-$DEFAULT_OUTPUT_FILE}
target_image=$1

mkdir -p "$(dirname "$output_file")"
touch "$output_file"

output_file_path=$(realpath "$output_file")

docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$output_file_path":/tmp/output.sarif \
  -u $(id -u):$(id -g) \
  "aquasec/trivy:$TRIVY_VERSION" \
  image \
  --cache-dir /tmp/.cache \
  --scanners vuln \
  --pkg-types os \
  --db-repository public.ecr.aws/aquasecurity/trivy-db \
  -o /tmp/output.sarif \
  --format sarif \
  "$target_image"

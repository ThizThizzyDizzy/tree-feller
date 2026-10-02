#!/usr/bin/env bash
set -euo pipefail

project_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)
local_config="$project_dir/test/mcautotester/local.env"
if [[ -f "$local_config" ]]; then
    source "$local_config"
fi
runner_home=${MCAUTOTESTER_HOME:-"$project_dir/../MCAutoTester/dist"}
runner_home=$(cd -- "$runner_home" && pwd)
if (($# == 0)); then
    set -- treefeller-acacia
fi

ant -q -f "$project_dir/Builder/build.xml" -Dmkdist.disabled=true jar
run_dir=$(mktemp -d "${TMPDIR:-/tmp}/treefeller-mcautotester.XXXXXXXX")
printf 'Run output and recordings: %s\n' "$run_dir"
cd -- "$run_dir"

{
    printf 'test'
    if [[ -n "${MCAUTOTESTER_HOST:-}" ]]; then
        printf ' -h "%s"' "$MCAUTOTESTER_HOST"
    fi
    printf ' -s "%s" -p "%s" -c' \
        "${MCAUTOTESTER_TARGET:-paper:1.21.5}" \
        "$project_dir/Builder/dist/treefeller-DEV.jar"
    printf ' -t "%s"' "$@"
    printf '\nexit\n'
} | java -Ddizzyengine.headless=true -jar "$runner_home/MCAutoTester.jar" 2>&1 | tee run.log

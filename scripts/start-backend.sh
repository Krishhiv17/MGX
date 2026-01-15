#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
pids=()

cleanup() {
  for pid in "${pids[@]:-}"; do
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid"
    fi
  done
}

trap cleanup EXIT INT TERM

rm -rf "${root_dir}/backend/target"

"${root_dir}/backend/mvnw" -f "${root_dir}/backend/pom.xml" spring-boot:run &
pids+=("$!")

"${root_dir}/backend/mvnw" -f "${root_dir}/services/bank-mock/pom.xml" spring-boot:run &
pids+=("$!")

"${root_dir}/backend/mvnw" -f "${root_dir}/services/fx-mock/pom.xml" spring-boot:run &
pids+=("$!")

wait

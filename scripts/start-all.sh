#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"${root_dir}/scripts/start-backend.sh" &
backend_pid="$!"

"${root_dir}/scripts/start-frontend.sh" &
frontend_pid="$!"

cleanup() {
  kill "$backend_pid" "$frontend_pid" 2>/dev/null || true
}

trap cleanup EXIT INT TERM

wait

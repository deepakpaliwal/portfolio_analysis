#!/usr/bin/env bash
#
# stop.sh - Stop portfolio-analysis services
# Usage: ./stop.sh [api|ui|batch|all]
# Default: all
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PID_DIR="$PROJECT_ROOT/logs/pids"
SHUTDOWN_TIMEOUT="${SHUTDOWN_TIMEOUT:-10}"   # seconds before SIGKILL

# ── helpers ──────────────────────────────────────────────────────────────────
usage() {
    cat <<EOF
Usage: $(basename "$0") [service]

Services:
  api     Stop portfolio-api
  ui      Stop portfolio-ui
  batch   Stop portfolio-batch
  all     Stop all services (default)

Environment variables:
  SHUTDOWN_TIMEOUT  Seconds to wait before SIGKILL (default: 10)
EOF
    exit 1
}

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

# Gracefully stop a process: SIGTERM -> wait -> SIGKILL
stop_process() {
    local name="$1"
    local pid=""

    # Try PID file first
    local pid_file="$PID_DIR/${name}.pid"
    if [[ -f "$pid_file" ]]; then
        pid="$(cat "$pid_file")"
    fi

    # Fallback: search by process name patterns
    if [[ -z "$pid" ]] || ! kill -0 "$pid" 2>/dev/null; then
        case "$name" in
            api)    pid=$(pgrep -f "portfolio-api" 2>/dev/null | head -1 || true) ;;
            ui)     pid=$(pgrep -f "vite.*portfolio-ui\|npm.*portfolio-ui" 2>/dev/null | head -1 || true) ;;
            batch)  pid=$(pgrep -f "portfolio-batch" 2>/dev/null | head -1 || true) ;;
        esac
    fi

    if [[ -z "$pid" ]]; then
        log "$name: not running (no PID found)"
        rm -f "$pid_file"
        return 0
    fi

    if ! kill -0 "$pid" 2>/dev/null; then
        log "$name: PID $pid is not running (stale PID file)"
        rm -f "$pid_file"
        return 0
    fi

    log "$name: sending SIGTERM to PID $pid ..."
    kill -TERM "$pid" 2>/dev/null || true

    # Wait for graceful shutdown
    local elapsed=0
    while kill -0 "$pid" 2>/dev/null && (( elapsed < SHUTDOWN_TIMEOUT )); do
        sleep 1
        (( elapsed++ ))
    done

    if kill -0 "$pid" 2>/dev/null; then
        log "$name: still running after ${SHUTDOWN_TIMEOUT}s, sending SIGKILL ..."
        kill -KILL "$pid" 2>/dev/null || true
        sleep 1
    fi

    if kill -0 "$pid" 2>/dev/null; then
        log "$name: WARNING - process $pid could not be killed"
        return 1
    fi

    log "$name: stopped (PID $pid)"
    rm -f "$pid_file"
    return 0
}

# ── main ─────────────────────────────────────────────────────────────────────
SERVICE="${1:-all}"

case "$SERVICE" in
    api)    stop_process api   ;;
    ui)     stop_process ui    ;;
    batch)  stop_process batch ;;
    all)
        stop_process api
        stop_process ui
        stop_process batch
        log "All services stopped."
        ;;
    -h|--help) usage ;;
    *)
        echo "ERROR: Unknown service '$SERVICE'"
        usage
        ;;
esac

#!/usr/bin/env bash
#
# start.sh - Start portfolio-analysis services
# Usage: ./start.sh [api|ui|batch|all]
# Default: all
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PROFILE="${PROFILE:-local}"
LOG_DIR="$PROJECT_ROOT/logs"
PID_DIR="$PROJECT_ROOT/logs/pids"

# ── helpers ──────────────────────────────────────────────────────────────────
usage() {
    cat <<EOF
Usage: $(basename "$0") [service]

Services:
  api     Start portfolio-api   (Spring Boot, port 8080)
  ui      Start portfolio-ui    (Vite dev server, port 5173)
  batch   Start portfolio-batch (Spring Batch/Spark, port 8082)
  all     Start all services in background (default)

Environment variables:
  PROFILE   Spring / build profile (default: local)

Examples:
  ./start.sh              # start everything
  PROFILE=dev ./start.sh api
EOF
    exit 1
}

ensure_dirs() {
    mkdir -p "$LOG_DIR" "$PID_DIR"
}

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

check_command() {
    if ! command -v "$1" &>/dev/null; then
        echo "ERROR: '$1' is not installed or not in PATH." >&2
        exit 1
    fi
}

# ── service starters ─────────────────────────────────────────────────────────
start_api() {
    log "Starting portfolio-api (profile=$PROFILE) ..."
    check_command mvn

    if [[ "${BACKGROUND:-false}" == "true" ]]; then
        nohup mvn spring-boot:run -f "$PROJECT_ROOT/pom.xml" -pl portfolio-api -P"${PROFILE}" \
            > "$LOG_DIR/api.log" 2>&1 &
        local pid=$!
        echo "$pid" > "$PID_DIR/api.pid"
        log "portfolio-api started  PID=$pid  log=$LOG_DIR/api.log"
    else
        mvn spring-boot:run -f "$PROJECT_ROOT/pom.xml" -pl portfolio-api -P"${PROFILE}"
    fi
}

start_ui() {
    log "Starting portfolio-ui (Vite dev server) ..."
    check_command npm

    if [[ "${BACKGROUND:-false}" == "true" ]]; then
        (cd "$PROJECT_ROOT/portfolio-ui" && nohup npm run dev \
            > "$LOG_DIR/ui.log" 2>&1 &
        local pid=$!
        echo "$pid" > "$PID_DIR/ui.pid"
        log "portfolio-ui  started  PID=$pid  log=$LOG_DIR/ui.log")
    else
        cd "$PROJECT_ROOT/portfolio-ui" && npm run dev
    fi
}

start_batch() {
    log "Starting portfolio-batch (profile=$PROFILE) ..."
    check_command mvn

    if [[ "${BACKGROUND:-false}" == "true" ]]; then
        nohup mvn spring-boot:run -f "$PROJECT_ROOT/pom.xml" -pl portfolio-batch -P"${PROFILE}" \
            > "$LOG_DIR/batch.log" 2>&1 &
        local pid=$!
        echo "$pid" > "$PID_DIR/batch.pid"
        log "portfolio-batch started  PID=$pid  log=$LOG_DIR/batch.log"
    else
        mvn spring-boot:run -f "$PROJECT_ROOT/pom.xml" -pl portfolio-batch -P"${PROFILE}"
    fi
}

start_all() {
    log "Starting ALL services in background (profile=$PROFILE) ..."
    BACKGROUND=true
    start_api
    start_ui
    start_batch
    echo ""
    log "All services launched. PID files in $PID_DIR"
    log "Logs directory: $LOG_DIR"
    echo ""
    echo "  api   -> $(cat "$PID_DIR/api.pid" 2>/dev/null || echo 'N/A')"
    echo "  ui    -> $(cat "$PID_DIR/ui.pid" 2>/dev/null || echo 'N/A')"
    echo "  batch -> $(cat "$PID_DIR/batch.pid" 2>/dev/null || echo 'N/A')"
}

# ── main ─────────────────────────────────────────────────────────────────────
ensure_dirs

SERVICE="${1:-all}"

case "$SERVICE" in
    api)    start_api   ;;
    ui)     start_ui    ;;
    batch)  start_batch ;;
    all)    start_all   ;;
    -h|--help) usage    ;;
    *)
        echo "ERROR: Unknown service '$SERVICE'"
        usage
        ;;
esac

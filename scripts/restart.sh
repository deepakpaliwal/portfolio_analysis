#!/usr/bin/env bash
#
# restart.sh - Restart portfolio-analysis services
# Usage: ./restart.sh [api|ui|batch|all]
# Default: all
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

SERVICE="${1:-all}"

usage() {
    cat <<EOF
Usage: $(basename "$0") [service]

Restarts the specified service (or all) by calling stop.sh then start.sh.

Services: api | ui | batch | all (default)
EOF
    exit 1
}

case "$SERVICE" in
    -h|--help) usage ;;
esac

echo "=== Stopping ${SERVICE} ==="
"$SCRIPT_DIR/stop.sh" "$SERVICE"

echo ""
echo "=== Starting ${SERVICE} ==="
"$SCRIPT_DIR/start.sh" "$SERVICE"

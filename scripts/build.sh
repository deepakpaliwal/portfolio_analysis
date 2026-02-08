#!/usr/bin/env bash
#
# build.sh - Build all portfolio-analysis modules
# Usage: ./build.sh [profile]
# Default profile: local
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PROFILE="${1:-local}"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

log "Building all modules (profile=$PROFILE) ..."

# Build Maven modules (API, Batch, DB)
log "Building Maven modules ..."
mvn clean package -f "$PROJECT_ROOT/pom.xml" -P"${PROFILE}" -DskipTests

# Build React UI
log "Building portfolio-ui ..."
cd "$PROJECT_ROOT/portfolio-ui"
if [ ! -d "node_modules" ]; then
    log "Installing npm dependencies ..."
    npm install
fi
npm run build

log "Build complete!"
log "  API jar:   portfolio-api/target/portfolio-api-*.jar"
log "  Batch jar: portfolio-batch/target/portfolio-batch-*.jar"
log "  UI dist:   portfolio-ui/dist/"

#!/usr/bin/env bash
#
# deploy-server.sh - Deploy portfolio-analysis to a remote Linux server
#
# Supports deploying individual modules to different physical servers.
#
# Usage:
#   ./deploy-server.sh --host <host> --module <api|ui|batch|all> [--profile <profile>] [--user <user>]
#
# Examples:
#   ./deploy-server.sh --host 192.168.1.100 --module all --profile dev
#   ./deploy-server.sh --host api-server.local --module api --profile prod
#   ./deploy-server.sh --host batch-server.local --module batch --profile prod
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Defaults
REMOTE_USER="${DEPLOY_USER:-deploy}"
REMOTE_HOST=""
MODULE="all"
PROFILE="dev"
REMOTE_BASE="/opt/portfolio-analysis"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

usage() {
    cat <<EOF
Usage: $(basename "$0") --host <host> --module <api|ui|batch|all> [options]

Options:
  --host    Target server hostname or IP (required)
  --module  Module to deploy: api, ui, batch, all (default: all)
  --profile Spring profile: local, dev, cat, prod (default: dev)
  --user    SSH user (default: deploy, or DEPLOY_USER env var)
  --path    Remote install path (default: /opt/portfolio-analysis)
  -h|--help Show this help

Examples:
  # Deploy all modules to one server
  $(basename "$0") --host 192.168.1.100 --module all --profile dev

  # Deploy API to dedicated server
  $(basename "$0") --host api.myserver.com --module api --profile prod

  # Deploy batch to a different server
  $(basename "$0") --host batch.myserver.com --module batch --profile prod
EOF
    exit 1
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        --host)    REMOTE_HOST="$2"; shift 2 ;;
        --module)  MODULE="$2"; shift 2 ;;
        --profile) PROFILE="$2"; shift 2 ;;
        --user)    REMOTE_USER="$2"; shift 2 ;;
        --path)    REMOTE_BASE="$2"; shift 2 ;;
        -h|--help) usage ;;
        *) echo "Unknown option: $1"; usage ;;
    esac
done

if [[ -z "$REMOTE_HOST" ]]; then
    echo "ERROR: --host is required"
    usage
fi

REMOTE="$REMOTE_USER@$REMOTE_HOST"

# Ensure remote directories exist
setup_remote() {
    log "Setting up remote directories on $REMOTE_HOST ..."
    ssh "$REMOTE" "mkdir -p $REMOTE_BASE/{api,ui,batch,db,logs,config}"
}

deploy_api() {
    local jar="$PROJECT_ROOT/portfolio-api/target/portfolio-api-1.0.0-SNAPSHOT.jar"
    if [[ ! -f "$jar" ]]; then
        log "Building API module ..."
        mvn package -f "$PROJECT_ROOT/pom.xml" -pl portfolio-db,portfolio-api -am -P"$PROFILE" -DskipTests
    fi
    log "Deploying portfolio-api to $REMOTE_HOST ..."
    scp "$jar" "$REMOTE:$REMOTE_BASE/api/portfolio-api.jar"

    # Deploy systemd service file
    ssh "$REMOTE" "cat > /tmp/portfolio-api.service" <<'SYSTEMD'
[Unit]
Description=Portfolio Analysis API
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=simple
User=deploy
Group=deploy
WorkingDirectory=/opt/portfolio-analysis/api
ExecStart=/usr/bin/java -jar /opt/portfolio-analysis/api/portfolio-api.jar --spring.profiles.active=${PROFILE}
ExecStop=/bin/kill -TERM $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/portfolio-analysis/logs/api.log
StandardError=append:/opt/portfolio-analysis/logs/api-error.log
Environment=SPRING_PROFILES_ACTIVE=${PROFILE}

[Install]
WantedBy=multi-user.target
SYSTEMD
    ssh "$REMOTE" "sudo mv /tmp/portfolio-api.service /etc/systemd/system/ && sudo systemctl daemon-reload && sudo systemctl enable portfolio-api && sudo systemctl restart portfolio-api"
    log "portfolio-api deployed and started on $REMOTE_HOST"
}

deploy_ui() {
    local dist="$PROJECT_ROOT/portfolio-ui/dist"
    if [[ ! -d "$dist" ]]; then
        log "Building UI module ..."
        (cd "$PROJECT_ROOT/portfolio-ui" && npm install && npm run build)
    fi
    log "Deploying portfolio-ui to $REMOTE_HOST ..."
    rsync -avz --delete "$dist/" "$REMOTE:$REMOTE_BASE/ui/"

    # Deploy nginx config
    ssh "$REMOTE" "cat > /tmp/portfolio-ui.conf" <<'NGINX'
server {
    listen 80;
    server_name _;
    root /opt/portfolio-analysis/ui;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /ws {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
NGINX
    ssh "$REMOTE" "sudo mv /tmp/portfolio-ui.conf /etc/nginx/conf.d/ && sudo nginx -t && sudo systemctl reload nginx"
    log "portfolio-ui deployed to $REMOTE_HOST (via nginx)"
}

deploy_batch() {
    local jar="$PROJECT_ROOT/portfolio-batch/target/portfolio-batch-1.0.0-SNAPSHOT.jar"
    if [[ ! -f "$jar" ]]; then
        log "Building Batch module ..."
        mvn package -f "$PROJECT_ROOT/pom.xml" -pl portfolio-db,portfolio-batch -am -P"$PROFILE" -DskipTests
    fi
    log "Deploying portfolio-batch to $REMOTE_HOST ..."
    scp "$jar" "$REMOTE:$REMOTE_BASE/batch/portfolio-batch.jar"

    ssh "$REMOTE" "cat > /tmp/portfolio-batch.service" <<'SYSTEMD'
[Unit]
Description=Portfolio Batch Processing
After=network.target postgresql.service

[Service]
Type=simple
User=deploy
Group=deploy
WorkingDirectory=/opt/portfolio-analysis/batch
ExecStart=/usr/bin/java -jar /opt/portfolio-analysis/batch/portfolio-batch.jar --spring.profiles.active=${PROFILE}
ExecStop=/bin/kill -TERM $MAINPID
Restart=on-failure
RestartSec=30
StandardOutput=append:/opt/portfolio-analysis/logs/batch.log
StandardError=append:/opt/portfolio-analysis/logs/batch-error.log
Environment=SPRING_PROFILES_ACTIVE=${PROFILE}

[Install]
WantedBy=multi-user.target
SYSTEMD
    ssh "$REMOTE" "sudo mv /tmp/portfolio-batch.service /etc/systemd/system/ && sudo systemctl daemon-reload && sudo systemctl enable portfolio-batch && sudo systemctl restart portfolio-batch"
    log "portfolio-batch deployed and started on $REMOTE_HOST"
}

# Main
setup_remote

case "$MODULE" in
    api)   deploy_api ;;
    ui)    deploy_ui ;;
    batch) deploy_batch ;;
    all)
        deploy_api
        deploy_ui
        deploy_batch
        ;;
    *) echo "ERROR: Unknown module '$MODULE'"; usage ;;
esac

log "Deployment to $REMOTE_HOST complete!"

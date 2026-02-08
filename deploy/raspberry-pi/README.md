# Raspberry Pi Cluster Deployment

Deploy the Portfolio Analysis application to a Raspberry Pi cluster.

## Prerequisites

- Raspberry Pi 4B (4GB+ RAM recommended) running Raspberry Pi OS (64-bit)
- Docker and Docker Compose installed on each node
- SSH access configured between nodes
- Static IP addresses or DNS configured

## Cluster Architecture

```
  [Pi-1: API + UI]  <-->  [Pi-2: Batch + Spark]  <-->  [Pi-3: PostgreSQL]
       :8080/:80              :8082                        :5432
```

Each module can run on a separate Pi or all on one.

## Quick Start (Single Node)

```bash
# On the Raspberry Pi:
cd /opt/portfolio-analysis
docker compose --profile dev up -d
```

## Multi-Node Deployment

```bash
# From your development machine:
# Deploy API to Pi-1
./scripts/deploy-server.sh --host pi-1.local --module api --profile dev

# Deploy Batch to Pi-2
./scripts/deploy-server.sh --host pi-2.local --module batch --profile dev

# Deploy UI to Pi-1 (or Pi-3)
./scripts/deploy-server.sh --host pi-1.local --module ui --profile dev
```

## Docker Compose (on the Pi)

```bash
# Copy the compose file
scp docker-compose.yml deploy@pi-1.local:/opt/portfolio-analysis/

# Copy the environment file
scp deploy/raspberry-pi/.env.rpi deploy@pi-1.local:/opt/portfolio-analysis/.env

# Start services
ssh deploy@pi-1.local "cd /opt/portfolio-analysis && docker compose --profile dev up -d"
```

## ARM64 Docker Builds

Build ARM64 images from your dev machine:

```bash
docker buildx build --platform linux/arm64 -t portfolio-api:arm64 -f portfolio-api/Dockerfile .
docker buildx build --platform linux/arm64 -t portfolio-batch:arm64 -f portfolio-batch/Dockerfile .
docker buildx build --platform linux/arm64 -t portfolio-ui:arm64 -f portfolio-ui/Dockerfile portfolio-ui/
```

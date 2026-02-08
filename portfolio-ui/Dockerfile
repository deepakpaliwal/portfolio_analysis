# ============================================================
# Portfolio UI - Multi-stage Docker build
# Supports: amd64 (AWS/x86), arm64 (Raspberry Pi/Apple Silicon)
# ============================================================
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

# Production image: serve via nginx
FROM nginx:alpine
LABEL maintainer="Portfolio Analysis Team"
LABEL description="Portfolio Analysis React UI"

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost/ || exit 1

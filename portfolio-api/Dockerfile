# ============================================================
# Portfolio API - Multi-stage Docker build
# Supports: amd64 (AWS/x86), arm64 (Raspberry Pi/Apple Silicon)
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY portfolio-db/pom.xml portfolio-db/pom.xml
COPY portfolio-api/pom.xml portfolio-api/pom.xml
# Download dependencies first (layer caching)
RUN mvn dependency:go-offline -pl portfolio-db,portfolio-api -am -B
COPY portfolio-db/src portfolio-db/src
COPY portfolio-api/src portfolio-api/src
RUN mvn package -pl portfolio-db,portfolio-api -am -DskipTests -B

# Runtime image
FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="Portfolio Analysis Team"
LABEL description="Portfolio Analysis REST API"

RUN addgroup -S portfolio && adduser -S portfolio -G portfolio

WORKDIR /app
COPY --from=build /app/portfolio-api/target/portfolio-api-*.jar app.jar

RUN chown -R portfolio:portfolio /app
USER portfolio

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

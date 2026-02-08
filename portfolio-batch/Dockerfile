# ============================================================
# Portfolio Batch - Multi-stage Docker build
# Supports: amd64 (AWS/x86), arm64 (Raspberry Pi/Apple Silicon)
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY portfolio-db/pom.xml portfolio-db/pom.xml
COPY portfolio-batch/pom.xml portfolio-batch/pom.xml
RUN mvn dependency:go-offline -pl portfolio-db,portfolio-batch -am -B
COPY portfolio-db/src portfolio-db/src
COPY portfolio-batch/src portfolio-batch/src
RUN mvn package -pl portfolio-db,portfolio-batch -am -DskipTests -B

# Runtime image
FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="Portfolio Analysis Team"
LABEL description="Portfolio Batch Processing (Spring Batch + Spark)"

RUN addgroup -S portfolio && adduser -S portfolio -G portfolio

WORKDIR /app
COPY --from=build /app/portfolio-batch/target/portfolio-batch-*.jar app.jar

RUN chown -R portfolio:portfolio /app
USER portfolio

EXPOSE 8082

HEALTHCHECK --interval=60s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

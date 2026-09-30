# syntax=docker/dockerfile:1
# ── Stage 1: Build JAR with Maven & Java 21 ─────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and package JAR (skipping unit tests for fast Docker build)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Runtime image (lightweight JRE 21) ─────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Install curl for Docker healthchecks and configure timezone
RUN apk add --no-cache curl tzdata \
    && addgroup -S spring && adduser -S spring -G spring

USER spring:spring

COPY --from=builder --chown=spring:spring /build/target/*.jar app.jar

ENV TZ=Europe/Moscow \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

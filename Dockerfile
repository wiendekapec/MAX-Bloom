FROM node:20-alpine AS frontend-builder

WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci --prefer-offline

COPY frontend/ ./
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder

WORKDIR /build
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

COPY backend/src ./src
COPY --from=frontend-builder /app/dist ./src/main/resources/static/

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN apk add --no-cache curl tzdata \
    && addgroup -S spring && adduser -S spring -G spring

USER spring:spring

COPY --from=backend-builder --chown=spring:spring /build/target/*.jar app.jar

ENV TZ=Europe/Moscow \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

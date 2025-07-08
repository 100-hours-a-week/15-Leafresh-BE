# 1단계: 빌드 스테이지
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle build --no-daemon

# 2단계: 실행 스테이지
FROM eclipse-temurin:21-jre-alpine

RUN curl -sSL https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "\
  java \
    -javaagent:/opt/otel/opentelemetry-javaagent.jar \
    -Dotel.exporter.otlp.endpoint=https://ingest.us.signoz.cloud/ \
    -Dotel.exporter.otlp.headers=signoz-ingestion-key=${SIGNOZ_INGESTION_KEY} \
    -Dotel.service.name=leafresh-backend \
    -Dotel.resource.attributes=deployment.environment=${DEPLOY_ENV:-dev} \
    -jar app.jar \
"]

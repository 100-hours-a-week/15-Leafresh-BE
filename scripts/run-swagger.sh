#!/bin/bash

# Swagger UI 로컬 실행 스크립트
# Usage: ./scripts/run-swagger.sh [port]

set -e

# 포트 설정 (기본값: 8080)
PORT=${1:-8080}

echo "🚀 Swagger UI 서버를 시작합니다..."
echo "📍 포트: $PORT"

# 프로젝트 루트 디렉토리로 이동
cd "$(dirname "$0")/.."

# 기존 프로세스 종료
echo "🧹 기존 프로세스 정리 중..."
pkill -f "spring.profiles.active=swagger" 2>/dev/null || true

# 애플리케이션 빌드
echo "🔨 애플리케이션 빌드 중..."
./gradlew build -x test

# Swagger 프로파일로 실행
echo "📄 Swagger UI 서버 시작 중..."
echo "⏳ 서버 시작까지 잠시 대기해주세요..."
echo ""
echo "🔗 Swagger UI: http://localhost:$PORT/swagger-ui.html"
echo "📋 OpenAPI JSON: http://localhost:$PORT/v3/api-docs"
echo ""
echo "⚠️  종료하려면 Ctrl+C를 눌러주세요"
echo ""

export SERVER_PORT=$PORT
./gradlew runSwagger

#!/bin/bash

# Swagger 문서 생성 스크립트
# Usage: ./scripts/generate-swagger.sh

set -e

echo "🚀 OpenAPI 문서 생성을 시작합니다..."

# 프로젝트 루트 디렉토리로 이동
cd "$(dirname "$0")/.."

# 기존 build 디렉토리 정리
echo "📁 기존 빌드 파일 정리 중..."
rm -rf build/openapi.json

# Gradle 빌드 및 OpenAPI 문서 생성
echo "🔨 애플리케이션 빌드 중..."
./gradlew clean build -x test

echo "📄 OpenAPI 문서 생성 중..."
# macOS에서 timeout 대신 background 실행 후 sleep 사용
./gradlew generateOpenApiDocs &
GRADLE_PID=$!

# 60초 대기
sleep 60

# 프로세스가 아직 실행중이면 종료
if kill -0 $GRADLE_PID 2>/dev/null; then
    echo "⚠️ 시간 초과로 프로세스를 종료합니다..."
    kill $GRADLE_PID 2>/dev/null || true
    sleep 2
    kill -9 $GRADLE_PID 2>/dev/null || true
fi

# 결과 확인
if [ -f "build/openapi.json" ]; then
    echo "✅ OpenAPI 문서 생성 완료!"
    echo "📍 파일 위치: build/openapi.json"
    echo "📊 파일 크기: $(du -h build/openapi.json | cut -f1)"
    echo ""
    echo "🔍 문서 정보:"
    if command -v jq &> /dev/null; then
        echo "  - Title: $(jq -r '.info.title // "N/A"' build/openapi.json)"
        echo "  - Version: $(jq -r '.info.version // "N/A"' build/openapi.json)"
        echo "  - Paths: $(jq '.paths | keys | length' build/openapi.json 2>/dev/null || echo "N/A")"
    else
        echo "  (jq가 설치되어 있지 않아 상세 정보를 표시할 수 없습니다)"
    fi
else
    echo "❌ OpenAPI 문서 생성에 실패했습니다."
    echo "💡 다음을 확인해보세요:"
    echo "   - 애플리케이션이 정상적으로 시작되는지 확인"
    echo "   - swagger 프로파일 설정이 올바른지 확인"
    echo "   - 외부 의존성(Redis, MySQL 등) 설정 확인"
    exit 1
fi

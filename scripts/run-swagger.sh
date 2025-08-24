#!/bin/bash

# Leafresh Swagger 문서 서버 실행 스크립트

set -e

echo "🌱 Leafresh API Swagger 문서 서버를 시작합니다..."

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 시스템 요구사항 확인
check_requirements() {
    echo "📋 시스템 요구사항을 확인합니다..."
    
    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ Java가 설치되지 않았습니다.${NC}"
        echo "Java 21을 설치해주세요."
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
    if [ "$java_version" -lt 21 ]; then
        echo -e "${RED}❌ Java 21 이상이 필요합니다. 현재 버전: Java $java_version${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Java $java_version 확인됨${NC}"
}

# 애플리케이션 빌드
build_application() {
    echo "🔨 애플리케이션을 빌드합니다..."
    
    if [ ! -f "./gradlew" ]; then
        echo -e "${RED}❌ gradlew 파일을 찾을 수 없습니다. 프로젝트 루트 디렉토리에서 실행해주세요.${NC}"
        exit 1
    fi
    
    chmod +x ./gradlew
    echo "JAR 파일 빌드 중..."
    ./gradlew clean bootJar -x test --quiet
    
    echo -e "${GREEN}✅ 빌드 완료${NC}"
}

# Swagger 서버 시작
start_swagger_server() {
    echo "🚀 Swagger 문서 서버를 시작합니다..."
    
    # JAR 파일 찾기
    JAR_FILE=$(find build/libs -name "*-SNAPSHOT.jar" -not -name "*-plain.jar" | head -n 1)
    
    if [ -z "$JAR_FILE" ]; then
        echo -e "${RED}❌ 실행 가능한 JAR 파일을 찾을 수 없습니다.${NC}"
        exit 1
    fi
    
    echo "JAR 파일: $JAR_FILE"
    
    # swagger 프로필로 서버 실행
    echo "swagger 프로필로 서버 시작 중..."
    java -jar "$JAR_FILE" \
        --spring.profiles.active=swagger \
        --server.port=8080 \
        --spring.main.banner-mode=off &
    
    server_pid=$!
    
    # 서버 시작 대기
    echo "⏳ 서버 시작을 기다립니다..."
    for i in {1..60}; do
        if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            break
        fi
        sleep 2
        echo -n "."
    done
    echo ""
    
    # 서버 상태 확인
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 서버가 성공적으로 시작되었습니다!${NC}"
        echo ""
        echo -e "${BLUE}📚 Swagger UI: ${YELLOW}http://localhost:8080/swagger-ui.html${NC}"
        echo -e "${BLUE}📄 API Docs JSON: ${YELLOW}http://localhost:8080/v3/api-docs${NC}"
        echo -e "${BLUE}🔍 Health Check: ${YELLOW}http://localhost:8080/actuator/health${NC}"
        echo ""
        echo -e "${YELLOW}💡 팁: Ctrl+C를 눌러 서버를 종료할 수 있습니다.${NC}"
        echo ""
        
        # 종료 시그널 처리
        trap "echo -e '\n${YELLOW}🛑 서버를 종료합니다...${NC}'; kill $server_pid; exit 0" INT TERM
        
        # 서버가 실행 중인 동안 대기
        wait $server_pid
    else
        echo -e "${RED}❌ 서버 시작에 실패했습니다.${NC}"
        kill $server_pid 2>/dev/null || true
        exit 1
    fi
}

# 메인 함수
main() {
    echo -e "${GREEN}"
    echo "╔══════════════════════════════════════╗"
    echo "║        🌱 Leafresh API Swagger       ║"
    echo "║      문서화 서버 for 완성된 API       ║"
    echo "╚══════════════════════════════════════╝"
    echo -e "${NC}"
    
    check_requirements
    build_application
    start_swagger_server
}

# 도움말 표시
show_help() {
    echo "Leafresh API Swagger 문서 서버 실행 스크립트"
    echo ""
    echo "사용법:"
    echo "  $0                 Swagger 서버 시작"
    echo "  $0 --help         도움말 표시"
    echo ""
    echo "완성된 Leafresh API의 문서화를 위한 서버입니다."
    echo "서버가 시작되면 다음 URL에서 API 문서를 확인할 수 있습니다:"
    echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "  - API JSON: http://localhost:8080/v3/api-docs"
}

# 인자 처리
case "${1:-}" in
    --help|-h)
        show_help
        exit 0
        ;;
    *)
        main
        ;;
esac

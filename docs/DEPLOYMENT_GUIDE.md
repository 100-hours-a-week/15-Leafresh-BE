# Leafresh API Documentation - Deployment Guide

🌱 이 가이드는 Leafresh API Swagger UI를 GitHub Pages로 배포하는 방법을 설명합니다.

## 📋 배포된 파일들

### GitHub Actions 워크플로우
- `.github/workflows/swagger-docs.yml` - 자동 Swagger 문서 생성 및 배포

### 문서 파일들
- `docs/index.html` - 메인 랜딩 페이지
- `docs/README.md` - 문서 설명
- `docs/_config.yml` - Jekyll 설정
- `docs/.nojekyll` - Jekyll 처리 비활성화
- `docs/DEPLOYMENT_GUIDE.md` - 이 파일

### 스크립트 파일들
- `scripts/run-swagger.sh` - 로컬 Swagger UI 실행
- `scripts/test-swagger.sh` - Swagger 엔드포인트 테스트

## 🚀 배포 프로세스

### 자동 배포
1. `main` 또는 `develop` 브랜치에 코드 푸시
2. GitHub Actions가 자동으로 실행됩니다:
   - Java 21 환경 설정
   - Gradle로 애플리케이션 빌드
   - Swagger 프로필로 애플리케이션 실행
   - OpenAPI 스펙 (`/v3/api-docs`) 다운로드
   - Swagger UI 정적 파일 생성
   - GitHub Pages에 배포

### 수동 배포
GitHub Actions의 "Generate and Deploy Swagger Documentation" 워크플로우를 수동으로 실행할 수 있습니다.

## 📖 접근 URL

배포된 문서는 다음 URL에서 확인할 수 있습니다:

- **메인 문서 페이지**: https://100-hours-a-week.github.io/15-Leafresh-BE/
- **Swagger UI**: https://100-hours-a-week.github.io/15-Leafresh-BE/swagger-ui.html
- **OpenAPI JSON**: https://100-hours-a-week.github.io/15-Leafresh-BE/openapi.json

## 🛠️ 로컬 개발

### Swagger UI 로컬 실행
```bash
# 편리한 스크립트 사용
./scripts/run-swagger.sh

# 또는 Gradle 태스크 직접 실행
./gradlew runSwagger
```

### 엔드포인트 테스트
```bash
# Swagger 엔드포인트 동작 확인
./scripts/test-swagger.sh
```

### 로컬 접근 URL
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- H2 Console: http://localhost:8080/h2-console

## 🔧 설정 상세

### GitHub Pages 설정
- **Source**: GitHub Actions
- **Custom domain**: 미설정 (기본 github.io 도메인 사용)
- **HTTPS**: 강제 활성화

### Swagger 프로필 설정
`application-swagger.yml`에 다음이 설정되어 있습니다:
- H2 인메모리 데이터베이스 사용
- 더미 환경 변수 설정
- 외부 의존성 Mock 활성화
- Swagger UI 및 OpenAPI 문서 생성 활성화

### GitHub Actions 권한
워크플로우는 다음 권한이 필요합니다:
- `contents: read` - 소스 코드 읽기
- `pages: write` - GitHub Pages 배포
- `id-token: write` - OIDC 토큰 생성

## 🔍 트러블슈팅

### 일반적인 문제들

**1. 애플리케이션 시작 실패**
- 로그 확인: GitHub Actions의 "Start application and generate OpenAPI spec" 단계
- 포트 충돌: 8080 포트가 이미 사용 중인지 확인
- Java 버전: Java 21이 올바르게 설치되었는지 확인

**2. OpenAPI 스펙 생성 실패**
- Health Check 확인: `/actuator/health` 엔드포인트 응답
- 타임아웃: 애플리케이션 시작 시간이 120초를 초과하는지 확인

**3. GitHub Pages 배포 실패**
- 권한 확인: Repository의 Settings > Actions > General에서 권한 설정
- Pages 설정: Settings > Pages에서 Source가 "GitHub Actions"로 설정되었는지 확인

### 디버깅 방법

**로컬에서 문제 재현**
```bash
# 1. 애플리케이션 수동 실행
./gradlew runSwagger

# 2. 다른 터미널에서 테스트
./scripts/test-swagger.sh

# 3. OpenAPI 스펙 수동 다운로드
curl -o docs/openapi.json http://localhost:8080/v3/api-docs
```

**GitHub Actions 로그 확인**
1. Repository > Actions 탭 이동
2. 실패한 워크플로우 클릭
3. 각 단계의 로그 상세 확인

## 📚 참고 자료

- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
- [GitHub Pages Documentation](https://docs.github.com/en/pages)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)

## 🤝 기여하기

문서 개선을 위한 제안이나 버그 리포트는 이슈로 등록해 주세요.

### 문서 업데이트 프로세스
1. 코드 변경 후 `main` 브랜치에 푸시
2. GitHub Actions가 자동으로 문서 업데이트
3. 5-10분 후 변경사항이 GitHub Pages에 반영

---

📝 **마지막 업데이트**: 2025년 8월 28일

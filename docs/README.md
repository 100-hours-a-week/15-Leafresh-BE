# Leafresh API Documentation

🌱 **환경보호를 실천하는 사람들을 위한 웹진자 플랫폼 Leafresh의 API 문서**

## 📖 문서 접근

- **[API 문서 (Swagger UI)](https://your-username.github.io/leafresh/)**
- **[OpenAPI 스펙 파일](./openapi.json)**

## 🚀 자동 업데이트

이 문서는 `main` 브랜치에 푸시될 때마다 GitHub Actions를 통해 자동으로 업데이트됩니다.

### 업데이트 과정

1. 최신 코드 체크아웃
2. Spring Boot 애플리케이션 빌드
3. Swagger 프로필로 애플리케이션 실행
4. OpenAPI 스펙 생성 (`/v3/api-docs`)
5. Swagger UI 정적 파일 생성
6. GitHub Pages에 배포

## 📚 API 정보

- **Title**: Leafresh API Documentation
- **Version**: 1.0.0
- **Base URL**: 
  - 프로덕션: `https://api.leafresh.app`
  - 개발: `https://springboot.dev-leafresh.app`

## 🔧 로컬 개발

```bash
# Swagger UI 로컬 실행
./gradlew runSwagger

# 브라우저에서 접근
open http://localhost:8080/swagger-ui.html
```

## 📝 문서 갱신

문서를 수동으로 갱신하려면 GitHub Actions에서 "Generate and Deploy Swagger Documentation" 워크플로우를 수동으로 실행할 수 있습니다.

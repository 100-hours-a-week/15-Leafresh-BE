# Leafresh

**AI-Powered Environmental Action Challenge Platform**

Leafresh is an environmental action platform that transforms sustainable behavior into engaging challenges with visual feedback and reward systems. The platform addresses common barriers to environmental action - lack of guidance, difficulty maintaining habits, and insufficient community support - by providing AI-powered verification, personalized recommendations, and social gamification. Users can participate in location and lifestyle-based challenges, verify their completion through AI-powered photo analysis, and earn "Leaf" points redeemable for eco-friendly products, creating sustained motivation for eco-friendly practices.

## Demo

[View System Demo](https://drive.google.com/file/d/1ZkqtZZAPD0C14j3FRuL-pETMJ2aOxl2n/view?usp=sharing)

## Features

### Core Capabilities

**Automated Verification System**
- Large language model-based image analysis for environmental activity validation
- Asynchronous processing pipeline with timeout and retry mechanisms
- Real-time feedback delivery via Server-Sent Events (SSE)

**Challenge Management**
- Individual and group challenge creation with customizable criteria
- Real-time progress tracking and leaderboard systems
- Category-based challenge browsing and filtering

**Marketplace & Rewards**
- Point-based economy using "Leaf" virtual currency
- Time-limited deal system with inventory management
- Transaction integrity with distributed locking mechanisms

**AI Chatbot**
- Self-hosted LLM pipeline for eco-friendly challenge recommendations
- Profile-based suggestions using location, workplace, and environmental interest categories
- Natural language processing for personalized challenge matching from user queries

**Community Platform**
- Real-time activity feeds with social interaction features
- User verification and feedback systems
- Achievement and badge system for user engagement

## Architecture

### System Overview

<img width="1264" height="865" alt="leafresh-v3-k8s" src="https://github.com/user-attachments/assets/8608b88f-e75d-499f-8cdd-75754465cfa3" />

### Design Principles

**Modular Monolith with Clean Architecture + Domain-Driven Design**

The system follows Clean Architecture principles with clear separation of concerns across four layers:

```
┌─ Presentation ─┐    ┌─ Application  ─┐    ┌─── Domain ───┐    ┌─ Infrastructure ─┐
│   Controllers  │ -> │   Services     │ -> │   Entities   │ <- │   Repositories   │
│   DTOs         │    │   Listeners    │    │   Events     │    │   Clients        │
│   Assemblers   │    │   DTOs         │    │   Services   │    │   Publishers     │
│   Utils        │    │                │    │   Support    │    │   Subscribers    │
│                │    │                │    │   Exceptions │    │   Cache          │
│                │    │                │    │              │    │   Schedulers     │
└────────────────┘    └────────────────┘    └──────────────┘    └──────────────────┘
```

**Domain Boundaries**

```
src/main/java/ktb/leafresh/backend/domain/
├── auth/                # Authentication & Authorization
├── verification/        # AI-based image verification
├── challenge/           # Challenge management system
│   ├── personal/        # Individual challenges
│   └── group/           # Group challenges
├── store/               # Marketplace and transactions
│   ├── product/         # Product catalog management
│   └── order/           # Order processing and fulfillment
├── member/              # User lifecycle management
├── feedback/            # AI feedback processing
├── notification/        # Push notification system
├── image/               # Image storage and management
└── chatbot/             # Conversational AI service
```

**Actual Package Structure**

```
domain/{domain-name}/
├── presentation/
│   ├── controller/    # REST API endpoints
│   ├── dto/           # Request/Response DTOs
│   ├── assembler/     # Data transformation logic
│   └── util/          # Presentation utilities
├── application/
│   ├── service/       # Application orchestration
│   ├── listener/      # Event listeners
│   └── dto/           # Internal DTOs
├── domain/
│   ├── entity/        # Domain entities
│   ├── service/       # Domain business logic
│   ├── event/         # Domain events
│   ├── support/       # Domain support classes
│   └── exception/     # Domain-specific exceptions
└── infrastructure/
    ├── repository/    # Data access implementations
    ├── client/        # External service clients
    ├── publisher/     # Message publishers
    ├── subscriber/    # Message subscribers
    ├── cache/         # Caching implementations
    ├── scheduler/     # Scheduled tasks
    └── dto/           # Infrastructure DTOs
```

**Key Patterns**
- Repository Pattern for data access abstraction
- Event-Driven Architecture for loose coupling
- Timeout and retry mechanisms for external service resilience
- Distributed locking for transaction integrity

## Tech Stack

### Backend
```
Java 21 • Spring Boot 3.4.5 • Spring Security 6.x • Spring Data JPA
QueryDSL 5.0 • Gradle 8.13 • Spring WebFlux
```

### Database & Cache
```
MySQL 8.0.41 • Cloud SQL (GCP) • RDS (AWS) • Redis 7.0 • Redisson 3.23.4 
RedisBloom 2.1.0 • HikariCP
```

### Infrastructure
```
Docker • Kubernetes • Helm 
GCP (Cloud Storage, Pub/Sub, Cloud SQL) • AWS (S3, SQS FIFO, RDS)
```

### Observability
```
OpenTelemetry • Prometheus • SigNoz • Micrometer • Logback
```

### Testing
```
JUnit 5 • Mockito • TestContainers • Spring REST Docs • JaCoCo • K6
```

### Development
```
IntelliJ IDEA • GitHub Actions • Docker Hub • SonarLint • Google Java Format
```

## Development Setup

### Prerequisites

- Java 21 (OpenJDK or Oracle JDK)
- Docker Desktop 4.0+
- IntelliJ IDEA 2024.1+ (recommended)
- Git 2.30+

### Quick Start

```bash
# Clone repository
git clone https://github.com/100-hours-a-week/15-Leafresh-BE.git
cd 15-Leafresh-BE

# Environment configuration
cp .env.example .env.local
# Configure required environment variables in .env.local

# Infrastructure startup
docker-compose -f docker-compose-local.yml up -d

# Application build and run
./gradlew build
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Development Tools Setup

**IDE Configuration**
```bash
# Code formatting
./gradlew spotlessApply

# Static analysis
./gradlew sonarLintMain
```

**Testing**
```bash
# Run all tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport

# Performance testing
./gradlew k6Test
```

## API Documentation

### Swagger UI Access

**Development Environment**
- Swagger UI: https://springboot.dev-leafresh.app/swagger-ui.html
- OpenAPI JSON: https://springboot.dev-leafresh.app/v3/api-docs

**Production Environment**
- Swagger UI: https://api.leafresh.app/swagger-ui.html
- OpenAPI JSON: https://api.leafresh.app/v3/api-docs

**Local Development**
```bash
# Run Swagger documentation server
./scripts/run-swagger.sh
# Access at http://localhost:8080/swagger-ui.html
```

**Generate Swagger Documentation**
```bash
# Build application and start documentation server
./gradlew runSwagger

# Or use the shell script for a better experience
chmod +x scripts/run-swagger.sh
./scripts/run-swagger.sh
```

### GitHub Pages Documentation

The API documentation is automatically deployed to GitHub Pages:
- **Live Documentation**: https://100-hours-a-week.github.io/15-Leafresh-BE/
- **Raw OpenAPI JSON**: https://100-hours-a-week.github.io/15-Leafresh-BE/swagger.json

The documentation is automatically updated whenever changes are pushed to the `main` or `develop` branches.

### API Overview

**Authentication Required**
Most endpoints require JWT token authentication. Include the following header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Main API Groups**
- **인증 (Auth)**: OAuth2 로그인, JWT 토큰 관리
- **멤버 (Members)**: 사용자 프로필, 배지, 포인트 관리
- **챌린지 (Challenges)**: 개인/그룹 챌린지 CRUD 및 참여
- **검증 (Verifications)**: AI 기반 이미지 검증 및 피드백
- **상점 (Store)**: 상품 관리 및 주문 처리
- **챗봇 (Chatbot)**: AI 기반 챌린지 추천
- **이미지 (Images)**: 파일 업로드 및 관리

## Deployment

### Container Build

```dockerfile
# Multi-stage build for optimized image size
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes Deployment

```yaml
# Production configuration
apiVersion: apps/v1
kind: Deployment
metadata:
  name: leafresh-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: leafresh-backend
  template:
    spec:
      containers:
      - name: backend
        image: leafresh/backend:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

## Monitoring

### Application Metrics
- JVM performance (heap, GC, thread pools)
- Business metrics (API latency, error rates, throughput)
- Custom metrics (challenge completion rates, verification accuracy)

### Infrastructure Monitoring
- Container resource utilization
- Database connection pool health
- Message queue depth and processing rates

### Alerting
- SLA violation detection
- Error rate threshold monitoring
- Resource exhaustion warnings

## Contributing

### Development Workflow

1. **Branch Strategy**: Git Flow with `main`, `develop`, and feature branches
2. **Commit Convention**: Conventional Commits specification
3. **Code Review**: Required for all changes with automated quality gates
4. **Testing**: Minimum 80% coverage requirement

### Code Quality Standards

- **Static Analysis**: SonarLint integration with quality gates
- **Formatting**: Google Java Style with automated enforcement
- **Documentation**: JavaDoc for public APIs, ADRs for architectural decisions

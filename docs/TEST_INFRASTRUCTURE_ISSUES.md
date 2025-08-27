# 테스트 인프라 문제 및 해결 방안

## 🚨 현재 문제 상황

### Repository 계층 통합 테스트 실패
- **문제**: H2 인메모리 데이터베이스에서 테이블이 생성되지 않음
- **에러**: `Table "tree_levels" not found`
- **영향**: GitHub Actions CI/CD 파이프라인에서 테스트 실패

## 🔍 분석된 원인

1. **엔티티 스캔 문제**: `@DataJpaTest`가 일부 엔티티 클래스를 제대로 스캔하지 못함
2. **Hibernate DDL 생성 실패**: H2에서 테이블이 자동 생성되지 않음
3. **Spring Boot 컨텍스트 로딩 이슈**: 테스트 환경에서의 Spring 컨텍스트 설정 문제

## ✅ 적용한 임시 해결책

### 1. CI/CD에서 단위 테스트만 실행
```yaml
- name: Run Unit Tests
  run: ./gradlew unitTest --continue
```

### 2. Gradle에 테스트 분리 태스크 추가
- `unitTest`: Service, Domain 계층만 테스트
- `integrationTest`: Repository 계층 테스트 (별도 실행)

## 🛠️ 향후 해결 방안

### 1. TestContainers 도입
```groovy
testImplementation 'org.testcontainers:mysql'
testImplementation 'org.testcontainers:junit-jupiter'
```

### 2. @SpringBootTest 활용
Repository 테스트에서 전체 Spring 컨텍스트 로딩:
```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MyRepositoryTest {
    // 전체 애플리케이션 컨텍스트와 함께 테스트
}
```

### 3. @Sql 어노테이션으로 스키마 초기화
```java
@Test
@Sql("/schema-test.sql")
void testRepository() {
    // 테스트 실행 전 스키마 초기화
}
```

### 4. 커스텀 TestConfiguration
```java
@TestConfiguration
@EnableAutoConfiguration
@EntityScan("ktb.leafresh.backend.**.domain.entity")
public class TestJpaConfiguration {
    // 명시적 설정
}
```

## 📋 체크리스트

### 단기 목표 (현재 상태)
- [x] CI/CD 파이프라인 안정화
- [x] 단위 테스트 분리 및 실행
- [x] Repository 테스트 별도 관리
- [ ] 통합 테스트 환경 수정

### 장기 목표
- [ ] TestContainers 도입
- [ ] 통합 테스트 안정화
- [ ] 테스트 데이터 관리 개선
- [ ] 테스트 성능 최적화

## 🚀 테스트 실행 방법

```bash
# 단위 테스트만 실행 (현재 CI/CD에서 사용)
./gradlew unitTest

# 전체 테스트 실행 (로컬에서만)
./gradlew test

# 통합 테스트만 실행 (문제 해결 후)
./gradlew integrationTest
```

## 📝 참고 자료

- [Spring Boot Testing Best Practices](https://spring.io/guides/gs/testing-web/)
- [TestContainers Documentation](https://testcontainers.org/)
- [H2 Database Configuration](http://h2database.com/)

---
**마지막 업데이트**: 2024-08-27
**상태**: 임시 해결 완료, 통합 테스트 개선 필요

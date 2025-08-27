package ktb.leafresh.backend.support.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 통합 테스트를 위한 메타 어노테이션
 * Spring Boot의 전체 컨텍스트를 로딩하여 JPA 엔티티 스캔과 DDL 생성을 보장
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.jpa.properties.hibernate.format_sql=false",
    "logging.level.org.hibernate=WARN",
    "logging.level.org.springframework=WARN",
    "logging.level.org.testcontainers=WARN"
})
@Transactional
public @interface IntegrationTestSupport {
}

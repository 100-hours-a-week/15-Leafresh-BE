package ktb.leafresh.backend.support.config;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

/**
 * JPA 테스트를 위한 설정 클래스
 * 엔티티 스캔 범위와 리포지토리 스캔 범위를 명시적으로 설정
 */
@TestConfiguration
@EntityScan(basePackages = "ktb.leafresh.backend.**.domain.entity")
@EnableJpaRepositories(basePackages = "ktb.leafresh.backend.**.infrastructure.repository")
@ComponentScan(basePackages = {
    "ktb.leafresh.backend.domain",
    "ktb.leafresh.backend.global.config",
    "ktb.leafresh.backend.support"
})
public class TestJpaConfig {
}

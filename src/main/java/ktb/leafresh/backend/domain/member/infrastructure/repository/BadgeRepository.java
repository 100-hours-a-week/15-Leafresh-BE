package ktb.leafresh.backend.domain.member.infrastructure.repository;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    // 뱃지 이름으로 중복 방지용 조회
    Optional<Badge> findByName(String name);
}

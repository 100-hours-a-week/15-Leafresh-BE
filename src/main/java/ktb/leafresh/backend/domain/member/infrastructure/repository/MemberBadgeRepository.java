package ktb.leafresh.backend.domain.member.infrastructure.repository;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberBadgeRepository
    extends JpaRepository<MemberBadge, Long>, MemberBadgeQueryRepository {
  // QueryDSL용 custom interface 분리

  boolean existsByMemberAndBadge(Member member, Badge badge);
}

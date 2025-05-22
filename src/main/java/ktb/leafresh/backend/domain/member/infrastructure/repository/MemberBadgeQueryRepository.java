package ktb.leafresh.backend.domain.member.infrastructure.repository;

import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;

import java.util.List;

public interface MemberBadgeQueryRepository {
    List<MemberBadge> findRecentBadgesByMemberId(Long memberId, int count);
}

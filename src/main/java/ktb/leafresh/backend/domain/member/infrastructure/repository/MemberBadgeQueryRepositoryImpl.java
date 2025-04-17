package ktb.leafresh.backend.domain.member.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;

import lombok.RequiredArgsConstructor;

import java.util.List;

import static ktb.leafresh.backend.domain.member.domain.entity.QMemberBadge.memberBadge;
import static ktb.leafresh.backend.domain.member.domain.entity.QBadge.badge;

@RequiredArgsConstructor
public class MemberBadgeQueryRepositoryImpl implements MemberBadgeQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberBadge> findRecentBadgesByMemberId(Long memberId, int count) {
        return queryFactory
                .selectFrom(memberBadge)
                .join(memberBadge.badge, badge).fetchJoin()
                .where(memberBadge.member.id.eq(memberId))
                .orderBy(memberBadge.acquiredAt.desc())
                .limit(count)
                .fetch();
    }
}

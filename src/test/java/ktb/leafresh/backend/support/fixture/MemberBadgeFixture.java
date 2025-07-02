package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;

import java.time.LocalDateTime;

public class MemberBadgeFixture {

    private static final LocalDateTime FIXED_ACQUIRED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * 기본 acquiredAt으로 MemberBadge 생성
     */
    public static MemberBadge of(Member member, Badge badge) {
        return of(member, badge, FIXED_ACQUIRED_AT);
    }

    /**
     * acquiredAt까지 지정하여 생성
     */
    public static MemberBadge of(Member member, Badge badge, LocalDateTime acquiredAt) {
        return MemberBadge.builder()
                .member(member)
                .badge(badge)
                .acquiredAt(acquiredAt)
                .build();
    }
}

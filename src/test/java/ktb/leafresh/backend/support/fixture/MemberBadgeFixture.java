package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;

import java.time.LocalDateTime;

public class MemberBadgeFixture {

    public static MemberBadge of(Member member, Badge badge) {
        return MemberBadge.of(member, badge);
    }

    public static MemberBadge of(Member member, Badge badge, LocalDateTime acquiredAt) {
        return MemberBadge.builder()
                .member(member)
                .badge(badge)
                .acquiredAt(acquiredAt)
                .build();
    }
}

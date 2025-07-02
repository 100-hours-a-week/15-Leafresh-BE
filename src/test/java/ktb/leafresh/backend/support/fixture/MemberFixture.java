package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.LoginType;
import ktb.leafresh.backend.domain.member.domain.entity.enums.Role;

import java.util.ArrayList;

public class MemberFixture {

    public static Member of() {
        return of("test@leafresh.com", "테스터");
    }

    public static Member of(String email, String nickname) {
        return Member.builder()
                .treeLevel(TreeLevelFixture.defaultLevel())
                .loginType(LoginType.SOCIAL)
                .email(email)
                .password(null)
                .nickname(nickname)
                .imageUrl("https://dummy.image/profile.png")
                .role(Role.USER)
                .activated(true)
                .totalLeafPoints(0)
                .currentLeafPoints(0)
                .memberBadges(new ArrayList<>())
                .auths(new ArrayList<>())
                .groupChallenges(new ArrayList<>())
                .groupChallengeParticipantRecords(new ArrayList<>())
                .personalChallengeVerifications(new ArrayList<>())
                .comments(new ArrayList<>())
                .likes(new ArrayList<>())
                .notifications(new ArrayList<>())
                .productPurchases(new ArrayList<>())
                .purchaseIdempotencyKeys(new ArrayList<>())
                .purchaseFailureLogs(new ArrayList<>())
                .feedbacks(new ArrayList<>())
                .build();
    }
}

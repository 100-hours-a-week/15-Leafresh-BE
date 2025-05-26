package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.member.domain.entity.Member;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class GroupChallengeFixture {

    public static GroupChallenge of(Member member, GroupChallengeCategory category) {
        return GroupChallenge.builder()
                .id(1L)
                .member(member)
                .category(category)
                .title("제로웨이스트 챌린지")
                .description("지속가능한 삶을 위한 실천")
                .imageUrl("https://dummy.image/challenge.png")
                .leafReward(10)
                .startDate(LocalDateTime.now().minusDays(7))
                .endDate(LocalDateTime.now().plusDays(7))
                .verificationStartTime(LocalTime.of(6, 0))
                .verificationEndTime(LocalTime.of(22, 0))
                .maxParticipantCount(100)
                .currentParticipantCount(10)
                .eventFlag(true)
                .build();
    }
}

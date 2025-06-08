package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;

public class GroupChallengeParticipantRecordFixture {

    public static GroupChallengeParticipantRecord of(GroupChallenge challenge, Member member) {
        return of(challenge, member, ParticipantStatus.ACTIVE);
    }

    public static GroupChallengeParticipantRecord of(GroupChallenge challenge, Member member, ParticipantStatus status) {
        return GroupChallengeParticipantRecord.builder()
                .id(1L)
                .groupChallenge(challenge)
                .member(member)
                .status(status)
                .bonusRewarded(false)
                .build();
    }
}

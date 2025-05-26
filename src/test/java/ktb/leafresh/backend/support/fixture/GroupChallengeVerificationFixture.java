package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;

public class GroupChallengeVerificationFixture {

    public static GroupChallengeVerification of(GroupChallengeParticipantRecord participantRecord) {
        return GroupChallengeVerification.builder()
                .participantRecord(participantRecord)
                .imageUrl("https://dummy.image/verify.jpg")
                .content("참여 인증")
                .status(ChallengeStatus.SUCCESS)
                .rewarded(true)
                .build();
    }
}

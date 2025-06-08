package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public class GroupChallengeVerificationFixture {

    public static GroupChallengeVerification of(GroupChallengeParticipantRecord participantRecord) {
        GroupChallengeVerification verification = GroupChallengeVerification.builder()
                .id(1L)
                .participantRecord(participantRecord)
                .imageUrl("https://dummy.image/verify.jpg")
                .content("참여 인증")
                .status(ChallengeStatus.SUCCESS)
                .rewarded(true)
                .viewCount(1)
                .likeCount(2)
                .commentCount(3)
                .build();

        ReflectionTestUtils.setField(verification, "createdAt", LocalDateTime.now());

        return verification;
    }
}

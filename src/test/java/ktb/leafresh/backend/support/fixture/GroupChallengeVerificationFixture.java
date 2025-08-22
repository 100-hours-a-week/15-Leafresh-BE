package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public class GroupChallengeVerificationFixture {

  private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

  /** 기본값으로 성공 인증을 생성합니다. */
  public static GroupChallengeVerification of(GroupChallengeParticipantRecord participantRecord) {
    return of(participantRecord, ChallengeStatus.SUCCESS);
  }

  /** 지정된 인증 상태로 인증 객체를 생성합니다. */
  public static GroupChallengeVerification of(
      GroupChallengeParticipantRecord participantRecord, ChallengeStatus status) {
    GroupChallengeVerification verification =
        GroupChallengeVerification.builder()
            .participantRecord(participantRecord)
            .imageUrl("https://dummy.image/verify.jpg")
            .content("참여 인증")
            .status(status)
            .rewarded(true)
            .build();

    ReflectionTestUtils.setField(verification, "createdAt", FIXED_TIME);
    return verification;
  }
}

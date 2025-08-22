package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;

import java.time.LocalDateTime;

public class PersonalChallengeVerificationFixture {

  private static final String DEFAULT_IMAGE_URL = "https://dummy.image/personal-verify.jpg";
  private static final String DEFAULT_CONTENT = "개인 챌린지 인증";
  private static final LocalDateTime DEFAULT_JOINED_AT = LocalDateTime.of(2025, 7, 1, 9, 0);
  private static final LocalDateTime DEFAULT_VERIFIED_AT = LocalDateTime.of(2025, 7, 1, 10, 0);
  private static final ChallengeStatus DEFAULT_STATUS = ChallengeStatus.SUCCESS;

  public static PersonalChallengeVerification of(Member member, PersonalChallenge challenge) {
    return PersonalChallengeVerification.builder()
        .member(member)
        .personalChallenge(challenge)
        .joinedAt(DEFAULT_JOINED_AT)
        .imageUrl(DEFAULT_IMAGE_URL)
        .content(DEFAULT_CONTENT)
        .status(DEFAULT_STATUS)
        .verifiedAt(DEFAULT_VERIFIED_AT)
        .rewarded(true)
        .build();
  }
}

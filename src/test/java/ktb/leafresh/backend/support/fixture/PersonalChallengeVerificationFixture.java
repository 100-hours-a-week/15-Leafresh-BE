package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;

import java.time.LocalDateTime;

public class PersonalChallengeVerificationFixture {

    public static PersonalChallengeVerification of(Member member, PersonalChallenge challenge) {
        return PersonalChallengeVerification.builder()
                .member(member)
                .personalChallenge(challenge)
                .joinedAt(LocalDateTime.now().minusDays(1))
                .imageUrl("https://dummy.image/personal-verify.jpg")
                .content("개인 챌린지 인증")
                .status(ChallengeStatus.SUCCESS)
                .verifiedAt(LocalDateTime.now())
                .rewarded(true)
                .build();
    }
}

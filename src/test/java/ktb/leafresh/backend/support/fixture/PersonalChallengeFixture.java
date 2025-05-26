package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;

import java.time.LocalTime;

public class PersonalChallengeFixture {

    public static PersonalChallenge of(String title) {
        return PersonalChallenge.builder()
                .id(1L)
                .title(title)
                .description("개인 챌린지 설명")
                .imageUrl("https://dummy.image/personal.png")
                .leafReward(5)
                .dayOfWeek(DayOfWeek.MONDAY)
                .verificationStartTime(LocalTime.of(5, 0))
                .verificationEndTime(LocalTime.of(23, 0))
                .build();
    }
}

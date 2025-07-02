package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;

import java.time.LocalTime;

public class PersonalChallengeFixture {

    private static final String DEFAULT_TITLE = "개인 챌린지 제목";
    private static final String DEFAULT_DESCRIPTION = "개인 챌린지 설명";
    private static final String DEFAULT_IMAGE_URL = "https://dummy.image/personal.png";
    private static final int DEFAULT_LEAF_REWARD = 5;
    private static final DayOfWeek DEFAULT_DAY = DayOfWeek.MONDAY;
    private static final LocalTime DEFAULT_START = LocalTime.of(5, 0);
    private static final LocalTime DEFAULT_END = LocalTime.of(23, 0);

    /**
     * 기본값으로 개인 챌린지를 생성합니다.
     */
    public static PersonalChallenge of() {
        return of(DEFAULT_TITLE);
    }

    /**
     * 지정된 제목으로 개인 챌린지를 생성합니다.
     */
    public static PersonalChallenge of(String title) {
        return PersonalChallenge.builder()
                .title(title)
                .description(DEFAULT_DESCRIPTION)
                .imageUrl(DEFAULT_IMAGE_URL)
                .leafReward(DEFAULT_LEAF_REWARD)
                .dayOfWeek(DEFAULT_DAY)
                .verificationStartTime(DEFAULT_START)
                .verificationEndTime(DEFAULT_END)
                .build();
    }
}

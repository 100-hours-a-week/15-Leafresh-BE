package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallengeExampleImage;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;

import java.util.List;

public class PersonalChallengeExampleImageFixture {

    public static List<PersonalChallengeExampleImage> list(PersonalChallenge challenge) {
        return List.of(
                PersonalChallengeExampleImage.of(
                        challenge,
                        "https://cdn.test.com/success.png",
                        ExampleImageType.SUCCESS,
                        "성공 예시",
                        1
                ),
                PersonalChallengeExampleImage.of(
                        challenge,
                        "https://cdn.test.com/fail.png",
                        ExampleImageType.FAILURE,
                        "실패 예시",
                        2
                )
        );
    }
}

package ktb.leafresh.backend.domain.challenge.personal.application.factory;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request.PersonalChallengeCreateRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalChallengeFactoryTest {

    private final PersonalChallengeFactory factory = new PersonalChallengeFactory();

    @Test
    @DisplayName("create_정상입력_도메인생성됨")
    void create_withValidInput_returnsPersonalChallenge() {
        // given
        PersonalChallengeCreateRequestDto.ExampleImageRequestDto image =
                new PersonalChallengeCreateRequestDto.ExampleImageRequestDto(
                        "https://test.image/example.jpg",
                        ExampleImageType.SUCCESS,
                        "예시 설명",
                        1
                );

        PersonalChallengeCreateRequestDto dto = new PersonalChallengeCreateRequestDto(
                "아침 기상 챌린지",
                "기상 후 인증샷 촬영",
                DayOfWeek.MONDAY,
                "https://test.image/thumbnail.jpg",
                LocalTime.of(6, 0),
                LocalTime.of(8, 0),
                List.of(image)
        );

        // when
        PersonalChallenge result = factory.create(dto);

        // then
        assertThat(result.getTitle()).isEqualTo(dto.title());
        assertThat(result.getDescription()).isEqualTo(dto.description());
        assertThat(result.getDayOfWeek()).isEqualTo(dto.dayOfWeek());
        assertThat(result.getImageUrl()).isEqualTo(dto.thumbnailImageUrl());
        assertThat(result.getVerificationStartTime()).isEqualTo(dto.verificationStartTime());
        assertThat(result.getVerificationEndTime()).isEqualTo(dto.verificationEndTime());
        assertThat(result.getLeafReward()).isEqualTo(30);
    }
}

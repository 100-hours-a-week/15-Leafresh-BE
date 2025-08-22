package ktb.leafresh.backend.domain.challenge.personal.application.factory;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallengeExampleImage;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request.PersonalChallengeCreateRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.support.fixture.PersonalChallengeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PersonalChallengeExampleImageAssemblerTest {

  @InjectMocks private PersonalChallengeExampleImageAssembler assembler;

  @Test
  @DisplayName("assemble_정상입력_예시이미지_연결됨")
  void assemble_withValidInput_attachesExampleImagesToChallenge() {
    // given
    PersonalChallenge challenge = PersonalChallengeFixture.of();

    PersonalChallengeCreateRequestDto.ExampleImageRequestDto image1 =
        new PersonalChallengeCreateRequestDto.ExampleImageRequestDto(
            "https://test-bucket/good1.jpg", ExampleImageType.SUCCESS, "성공 예시 이미지입니다", 1);

    PersonalChallengeCreateRequestDto.ExampleImageRequestDto image2 =
        new PersonalChallengeCreateRequestDto.ExampleImageRequestDto(
            "https://test-bucket/fail1.jpg", ExampleImageType.FAILURE, "실패 예시 이미지입니다", 2);

    PersonalChallengeCreateRequestDto requestDto =
        new PersonalChallengeCreateRequestDto(
            "아침 6시 기상",
            "기상 후 10분 내 사진 인증",
            DayOfWeek.MONDAY,
            "https://test-bucket/thumb.jpg",
            LocalTime.of(6, 0),
            LocalTime.of(8, 0),
            List.of(image1, image2));

    // when
    assembler.assemble(challenge, requestDto);

    // then
    List<PersonalChallengeExampleImage> exampleImages = challenge.getExampleImages();
    assertThat(exampleImages).hasSize(2);

    PersonalChallengeExampleImage first = exampleImages.get(0);
    assertThat(first.getImageUrl()).isEqualTo(image1.imageUrl());
    assertThat(first.getType()).isEqualTo(image1.type());
    assertThat(first.getDescription()).isEqualTo(image1.description());
    assertThat(first.getSequenceNumber()).isEqualTo(image1.sequenceNumber());
    assertThat(first.getPersonalChallenge()).isEqualTo(challenge);

    PersonalChallengeExampleImage second = exampleImages.get(1);
    assertThat(second.getImageUrl()).isEqualTo(image2.imageUrl());
    assertThat(second.getType()).isEqualTo(image2.type());
    assertThat(second.getDescription()).isEqualTo(image2.description());
    assertThat(second.getSequenceNumber()).isEqualTo(image2.sequenceNumber());
    assertThat(second.getPersonalChallenge()).isEqualTo(challenge);
  }
}

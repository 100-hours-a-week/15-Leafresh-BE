package ktb.leafresh.backend.domain.challenge.personal.application.service;

import ktb.leafresh.backend.domain.challenge.personal.application.factory.PersonalChallengeExampleImageAssembler;
import ktb.leafresh.backend.domain.challenge.personal.application.factory.PersonalChallengeFactory;
import ktb.leafresh.backend.domain.challenge.personal.application.validator.PersonalChallengeDomainValidator;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request.PersonalChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request.PersonalChallengeCreateRequestDto.ExampleImageRequestDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeCreateResponseDto;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.support.fixture.PersonalChallengeFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("PersonalChallengeCreateService 테스트")
class PersonalChallengeCreateServiceTest {

  @Mock private PersonalChallengeDomainValidator validator;

  @Mock private PersonalChallengeFactory factory;

  @Mock private PersonalChallengeExampleImageAssembler assembler;

  @Mock private PersonalChallengeRepository repository;

  @InjectMocks private PersonalChallengeCreateService service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @DisplayName("정상적인 요청으로 개인 챌린지를 생성할 수 있다")
  @Test
  void createPersonalChallenge_withValidRequest_returnsResponseDto() {
    // given
    PersonalChallengeCreateRequestDto request =
        new PersonalChallengeCreateRequestDto(
            "매일 스트레칭하기",
            "매일 아침 10분 스트레칭",
            DayOfWeek.MONDAY,
            "https://cdn.test.com/thumbnail.png",
            LocalTime.of(6, 0),
            LocalTime.of(22, 0),
            List.of(
                new ExampleImageRequestDto(
                    "https://cdn.test.com/image1.png", ExampleImageType.SUCCESS, "성공 예시", 1)));

    PersonalChallenge created = PersonalChallengeFixture.of("매일 스트레칭하기");
    ReflectionTestUtils.setField(created, "id", 10L);

    given(factory.create(request)).willReturn(created);
    willDoNothing().given(assembler).assemble(created, request);
    given(repository.save(created)).willReturn(created);

    // when
    PersonalChallengeCreateResponseDto response = service.create(request);

    // then
    assertThat(response.id()).isEqualTo(created.getId());
    then(validator).should().validate(DayOfWeek.MONDAY);
    then(factory).should().create(request);
    then(assembler).should().assemble(created, request);
    then(repository).should().save(created);
  }

  @DisplayName("잘못된 요일 입력 시 예외가 발생한다")
  @Test
  void createPersonalChallenge_withInvalidDayOfWeek_throwsException() {
    // given
    PersonalChallengeCreateRequestDto request =
        new PersonalChallengeCreateRequestDto(
            "잘못된 챌린지",
            "잘못된 설명",
            DayOfWeek.FRIDAY,
            "https://cdn.test.com/image.png",
            LocalTime.of(0, 0),
            LocalTime.of(1, 0),
            List.of());

    willThrow(new IllegalArgumentException("유효하지 않은 요일입니다."))
        .given(validator)
        .validate(DayOfWeek.FRIDAY);

    // when & then
    assertThatThrownBy(() -> service.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("유효하지 않은 요일");

    then(factory).should(never()).create(any());
    then(repository).shouldHaveNoInteractions();
  }
}

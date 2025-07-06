package ktb.leafresh.backend.domain.challenge.group.application.validator;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.client.AiChallengeValidationClient;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.request.AiChallengeValidationRequestDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.response.AiChallengeValidationResponseDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto.ExampleImageRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiChallengePolicyValidator 테스트")
class AiChallengePolicyValidatorTest {

    @Mock
    private AiChallengeValidationClient aiChallengeValidationClient;

    @Mock
    private GroupChallengeRepository groupChallengeRepository;

    @InjectMocks
    private AiChallengePolicyValidator aiChallengePolicyValidator;

    private GroupChallengeCreateRequestDto validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new GroupChallengeCreateRequestDto(
                "제로웨이스트 챌린지",
                "지속가능한 삶을 위한 실천",
                "ZERO_WASTE",
                100,
                "https://dummy.image/challenge.png",
                OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, UTC),
                OffsetDateTime.of(2025, 1, 7, 23, 59, 0, 0, UTC),
                LocalTime.of(6, 0),
                LocalTime.of(22, 0),
                List.of(new ExampleImageRequestDto("https://dummy.image/img.png", ExampleImageType.SUCCESS, "성공 예시", 1))
        );
    }

    @Nested
    @DisplayName("validate() 정상 케이스")
    class ValidateSuccess {

        @Test
        @DisplayName("유효한 요청일 경우 예외 없이 검증 통과")
        void validate_withValidInput_shouldPass() {
            // given
            Long memberId = 1L;
            GroupChallenge dummyChallenge = GroupChallengeFixture.of(null, null);
            given(groupChallengeRepository.findAllValidAndOngoing(any(LocalDateTime.class)))
                    .willReturn(List.of(dummyChallenge));
            given(aiChallengeValidationClient.validateChallenge(any(AiChallengeValidationRequestDto.class)))
                    .willReturn(new AiChallengeValidationResponseDto(true));

            // when & then
            assertThatCode(() -> aiChallengePolicyValidator.validate(memberId, validRequest))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validate() 필수 항목 검증 실패")
    class ValidateRequiredFieldFailure {

        @Test
        @DisplayName("memberId가 null이면 예외 발생")
        void validate_withNullMemberId_shouldThrowException() {
            assertThatThrownBy(() -> aiChallengePolicyValidator.validate(null, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_MISSING_MEMBER_ID.getMessage());
        }

        @Test
        @DisplayName("title이 null이면 예외 발생")
        void validate_withNullTitle_shouldThrowException() {
            GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                    null, validRequest.description(), validRequest.category(),
                    validRequest.maxParticipantCount(), validRequest.thumbnailImageUrl(),
                    validRequest.startDate(), validRequest.endDate(),
                    validRequest.verificationStartTime(), validRequest.verificationEndTime(),
                    validRequest.exampleImages()
            );

            assertThatThrownBy(() -> aiChallengePolicyValidator.validate(1L, dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_MISSING_TITLE.getMessage());
        }

        @Test
        @DisplayName("startDate가 null이면 예외 발생")
        void validate_withNullStartDate_shouldThrowException() {
            GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                    validRequest.title(), validRequest.description(), validRequest.category(),
                    validRequest.maxParticipantCount(), validRequest.thumbnailImageUrl(),
                    null, validRequest.endDate(),
                    validRequest.verificationStartTime(), validRequest.verificationEndTime(),
                    validRequest.exampleImages()
            );

            assertThatThrownBy(() -> aiChallengePolicyValidator.validate(1L, dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_MISSING_START_DATE.getMessage());
        }

        @Test
        @DisplayName("endDate가 null이면 예외 발생")
        void validate_withNullEndDate_shouldThrowException() {
            GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                    validRequest.title(), validRequest.description(), validRequest.category(),
                    validRequest.maxParticipantCount(), validRequest.thumbnailImageUrl(),
                    validRequest.startDate(), null,
                    validRequest.verificationStartTime(), validRequest.verificationEndTime(),
                    validRequest.exampleImages()
            );

            assertThatThrownBy(() -> aiChallengePolicyValidator.validate(1L, dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_MISSING_END_DATE.getMessage());
        }
    }

    @Nested
    @DisplayName("validate() AI 거절 또는 예외 케이스")
    class ValidateAiFailure {

        @Test
        @DisplayName("AI 응답 결과가 false이면 예외 발생")
        void validate_whenAiRejects_shouldThrowException() {
            // given
            given(groupChallengeRepository.findAllValidAndOngoing(any())).willReturn(List.of());
            given(aiChallengeValidationClient.validateChallenge(any()))
                    .willReturn(new AiChallengeValidationResponseDto(false));

            // when & then
            assertThatThrownBy(() -> aiChallengePolicyValidator.validate(1L, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_PROCESSING_FAILED.getMessage());
        }

        @Test
        @DisplayName("예외 발생 시 AI 처리 실패 예외로 전환")
        void validate_whenClientThrows_shouldThrowProcessingFailed() {
            given(groupChallengeRepository.findAllValidAndOngoing(LocalDateTime.now())).willReturn(List.of());
            given(aiChallengeValidationClient.validateChallenge(any()))
                    .willThrow(new RuntimeException("AI 서버 오류"));

            assertThatThrownBy(() -> aiChallengePolicyValidator.validate(1L, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_PROCESSING_FAILED.getMessage());
        }
    }
}

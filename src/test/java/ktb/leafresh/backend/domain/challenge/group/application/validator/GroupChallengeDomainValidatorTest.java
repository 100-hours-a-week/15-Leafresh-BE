package ktb.leafresh.backend.domain.challenge.group.application.validator;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto.ExampleImageRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeDomainValidator 테스트")
class GroupChallengeDomainValidatorTest {

    @Mock
    private GroupChallengeCategoryRepository categoryRepository;

    @InjectMocks
    private GroupChallengeDomainValidator validator;

    private static final OffsetDateTime START = OffsetDateTime.of(2025, 7, 1, 0, 0, 0, 0, UTC);
    private static final OffsetDateTime END = OffsetDateTime.of(2025, 7, 7, 23, 59, 0, 0, UTC);
    private static final LocalTime VERIFY_START = LocalTime.of(6, 0);
    private static final LocalTime VERIFY_END = LocalTime.of(22, 0);

    private GroupChallengeCreateRequestDto baseDto;

    @BeforeEach
    void setUp() {
        baseDto = new GroupChallengeCreateRequestDto(
                "제로웨이스트 챌린지",
                "설명",
                "ZERO_WASTE",
                100,
                "https://dummy.image/thumb.png",
                START,
                END,
                VERIFY_START,
                VERIFY_END,
                List.of(new ExampleImageRequestDto("https://dummy.image/img1.png", ExampleImageType.SUCCESS, "성공 예시", 1))
        );

        GroupChallengeCategory category = GroupChallengeCategory.builder()
                .name("ZERO_WASTE")
                .imageUrl(GroupChallengeCategoryName.getImageUrl("ZERO_WASTE"))
                .sequenceNumber(GroupChallengeCategoryName.getSequence("ZERO_WASTE"))
                .activated(true)
                .build();

        lenient().when(categoryRepository.findByName("ZERO_WASTE"))
                .thenReturn(Optional.of(category));
    }

    @Nested
    @DisplayName("성공 케이스")
    class Success {

        @Test
        @DisplayName("정상 요청 시 예외 발생하지 않음")
        void validate_withValidInput_shouldPass() {
            assertThatCode(() -> validator.validate(baseDto)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("실패 케이스 - 공통 검증")
    class CommonValidationFailure {

        @Test
        @DisplayName("종료일이 시작일보다 이전이면 예외 발생")
        void validate_withInvalidDateRange_shouldThrow() {
            GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                    baseDto.title(), baseDto.description(), baseDto.category(),
                    baseDto.maxParticipantCount(), baseDto.thumbnailImageUrl(),
                    END, START, VERIFY_START, VERIFY_END, baseDto.exampleImages()
            );

            assertThatThrownBy(() -> validator.validate(dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.INVALID_DATE_RANGE.getMessage());
        }

        @Test
        @DisplayName("챌린지 기간이 하루 미만이면 예외 발생")
        void validate_withTooShortDuration_shouldThrow() {
            GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                    baseDto.title(), baseDto.description(), baseDto.category(),
                    baseDto.maxParticipantCount(), baseDto.thumbnailImageUrl(),
                    START, START.plusHours(12), VERIFY_START, VERIFY_END, baseDto.exampleImages()
            );

            assertThatThrownBy(() -> validator.validate(dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.CHALLENGE_DURATION_TOO_SHORT.getMessage());
        }

        @Test
        @DisplayName("인증 종료 시간이 시작 시간보다 빠르면 예외 발생")
        void validate_withInvalidVerificationTime_shouldThrow() {
            GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                    baseDto.title(), baseDto.description(), baseDto.category(),
                    baseDto.maxParticipantCount(), baseDto.thumbnailImageUrl(),
                    START, END, VERIFY_END, VERIFY_START, baseDto.exampleImages()
            );

            assertThatThrownBy(() -> validator.validate(dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.INVALID_VERIFICATION_TIME.getMessage());
        }

        @Test
        @DisplayName("인증 시간 차이가 10분 미만이면 예외 발생")
        void validate_withShortVerificationDuration_shouldThrow() {
            LocalTime shortEnd = VERIFY_START.plusMinutes(5);
            GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                    baseDto.title(), baseDto.description(), baseDto.category(),
                    baseDto.maxParticipantCount(), baseDto.thumbnailImageUrl(),
                    START, END, VERIFY_START, shortEnd, baseDto.exampleImages()
            );

            assertThatThrownBy(() -> validator.validate(dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.VERIFICATION_DURATION_TOO_SHORT.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 카테고리이면 예외 발생")
        void validate_withInvalidCategory_shouldThrow() {
            given(categoryRepository.findByName("INVALID_CATEGORY")).willReturn(Optional.empty());

            GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                    baseDto.title(), baseDto.description(), "INVALID_CATEGORY",
                    baseDto.maxParticipantCount(), baseDto.thumbnailImageUrl(),
                    START, END, VERIFY_START, VERIFY_END, baseDto.exampleImages()
            );

            assertThatThrownBy(() -> validator.validate(dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.CHALLENGE_CATEGORY_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("실패 케이스 - 예시 이미지")
    class ImageValidationFailure {

        @Test
        @DisplayName("예시 이미지가 없으면 예외 발생")
        void validate_withEmptyImages_shouldThrow() {
            GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                    baseDto.title(), baseDto.description(), baseDto.category(),
                    baseDto.maxParticipantCount(), baseDto.thumbnailImageUrl(),
                    START, END, VERIFY_START, VERIFY_END, List.of()
            );

            assertThatThrownBy(() -> validator.validate(dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_REQUIRES_SUCCESS_IMAGE.getMessage());
        }

        @Test
        @DisplayName("성공 이미지가 하나도 없으면 예외 발생")
        void validate_withNoSuccessImage_shouldThrow() {
            GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                    baseDto.title(), baseDto.description(), baseDto.category(),
                    baseDto.maxParticipantCount(), baseDto.thumbnailImageUrl(),
                    START, END, VERIFY_START, VERIFY_END,
                    List.of(new ExampleImageRequestDto("https://dummy.image/img.png", ExampleImageType.FAILURE, "실패 예시", 1))
            );

            assertThatThrownBy(() -> validator.validate(dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_REQUIRES_SUCCESS_IMAGE.getMessage());
        }
    }

    @Nested
    @DisplayName("실패 케이스 - 업데이트 시 성공 이미지 없음")
    class UpdateImageValidationFailure {

        @Test
        @DisplayName("신규 성공 이미지 없고 기존 유지 이미지도 없으면 예외 발생")
        void validateUpdate_withNoSuccessImage_shouldThrow() {
            GroupChallengeUpdateRequestDto dto = new GroupChallengeUpdateRequestDto(
                    "제로웨이스트 챌린지",
                    "설명",
                    "ZERO_WASTE",
                    100,
                    "https://dummy.image/thumb.png",
                    START,
                    END,
                    VERIFY_START,
                    VERIFY_END,
                    new GroupChallengeUpdateRequestDto.ExampleImages(
                            List.of(), // keep 없음
                            List.of(new GroupChallengeUpdateRequestDto.ExampleImages.NewImage(
                                    "https://dummy.image/fail.png",
                                    ExampleImageType.FAILURE,
                                    "실패 예시",
                                    1
                            )),
                            List.of() // deleted 없음
                    )
            );

            assertThatThrownBy(() -> validator.validate(dto))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_REQUIRES_SUCCESS_IMAGE.getMessage());
        }
    }
}

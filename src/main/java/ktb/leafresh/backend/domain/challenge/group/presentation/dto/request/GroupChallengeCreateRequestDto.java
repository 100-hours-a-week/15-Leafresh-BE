package ktb.leafresh.backend.domain.challenge.group.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.validator.ValidImageUrl;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "단체 챌린지 생성 요청")
public record GroupChallengeCreateRequestDto(
    @NotBlank @Schema(description = "챌린지 제목", example = "제로웨이스트 챌린지") String title,
    @NotBlank @Schema(description = "챌린지 설명", example = "환경을 위한 제로웨이스트 챌린지에 참여해보세요!")
        String description,
    @NotBlank @Schema(description = "카테고리", example = "ZERO_WASTE") String category,
    @Positive @Schema(description = "최대 참여 인원 수", example = "50") int maxParticipantCount,
    @NotBlank
        @ValidImageUrl
        @Schema(description = "썸네일 이미지 URL", example = "https://leafresh.io/thumbnail.jpg")
        String thumbnailImageUrl,
    @NotNull @Schema(description = "챌린지 시작일", example = "2024-01-01T00:00:00+09:00")
        OffsetDateTime startDate,
    @NotNull @Schema(description = "챌린지 종료일", example = "2024-01-31T23:59:59+09:00")
        OffsetDateTime endDate,
    @NotNull @Schema(description = "인증 시작 시간", example = "09:00") LocalTime verificationStartTime,
    @NotNull @Schema(description = "인증 종료 시간", example = "21:00") LocalTime verificationEndTime,
    @Valid
        @NotNull(message = "챌린지에는 최소 한 개 이상의 성공 예시 이미지가 필요합니다.")
        @Size(max = 5)
        @Schema(description = "인증 예시 이미지 목록 (최대 5개)")
        List<ExampleImageRequestDto> exampleImages) {

  @Schema(description = "예시 이미지 정보")
  public record ExampleImageRequestDto(
      @NotBlank
          @ValidImageUrl
          @Schema(description = "예시 이미지 URL", example = "https://leafresh.io/example.jpg")
          String imageUrl,
      @NotNull @Schema(description = "예시 이미지 타입", example = "GOOD") ExampleImageType type,
      @NotBlank @Schema(description = "예시 이미지 설명", example = "올바른 인증 예시") String description,
      @Min(1) @Schema(description = "순서 번호", example = "1") int sequenceNumber) {}
}

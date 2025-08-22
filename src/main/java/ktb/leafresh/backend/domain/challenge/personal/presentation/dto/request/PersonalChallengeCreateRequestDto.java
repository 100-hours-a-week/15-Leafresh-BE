package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.validator.ValidImageUrl;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "개인 챌린지 생성 요청 DTO")
public record PersonalChallengeCreateRequestDto(
    @NotBlank @Schema(description = "제목") String title,
    @NotBlank @Schema(description = "설명") String description,
    @NotNull @Schema(description = "요일") DayOfWeek dayOfWeek,
    @NotBlank @Schema(description = "썸네일 이미지 URL") String thumbnailImageUrl,
    @NotNull @Schema(description = "인증 시작 시간") LocalTime verificationStartTime,
    @NotNull @Schema(description = "인증 종료 시간") LocalTime verificationEndTime,
    @Size(max = 5) @Schema(description = "인증 예시 이미지 목록")
        List<ExampleImageRequestDto> exampleImages) {
  public record ExampleImageRequestDto(
      @NotBlank @ValidImageUrl String imageUrl,
      @NotNull ExampleImageType type,
      @NotBlank String description,
      @Min(1) int sequenceNumber) {}
}

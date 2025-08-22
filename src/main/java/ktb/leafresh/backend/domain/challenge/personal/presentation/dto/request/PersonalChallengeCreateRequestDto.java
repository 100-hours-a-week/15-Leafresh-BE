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
    @NotBlank @Schema(description = "챌린지 제목", example = "매일 물 8잔 마시기") String title,
    @NotBlank @Schema(description = "챌린지 설명", example = "건강한 하루를 위해 물 8잔을 마셔보세요!")
        String description,
    @NotNull @Schema(description = "챌린지 실행 요일", example = "MONDAY") DayOfWeek dayOfWeek,
    @NotBlank
        @ValidImageUrl
        @Schema(description = "썸네일 이미지 URL", example = "https://leafresh.io/thumbnail.jpg")
        String thumbnailImageUrl,
    @NotNull @Schema(description = "인증 시작 시간", example = "09:00") LocalTime verificationStartTime,
    @NotNull @Schema(description = "인증 종료 시간", example = "21:00") LocalTime verificationEndTime,
    @Size(max = 5) @Schema(description = "인증 예시 이미지 목록 (최대 5개)")
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

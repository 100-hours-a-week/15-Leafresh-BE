package ktb.leafresh.backend.domain.challenge.group.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.validator.ValidImageUrl;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "단체 챌린지 수정 요청")
public record GroupChallengeUpdateRequestDto(
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
    @NotNull @Valid @Schema(description = "인증 예시 이미지 수정 정보") ExampleImages exampleImages) {

  @Schema(description = "예시 이미지 수정 정보")
  public record ExampleImages(
      @Size(max = 5)
          @JsonSetter(nulls = Nulls.AS_EMPTY)
          @Schema(description = "기존 유지할 이미지 ID와 순서 목록")
          List<KeepImage> keep,
      @JsonProperty("new")
          @JsonSetter(nulls = Nulls.AS_EMPTY)
          @Schema(description = "신규 추가할 이미지 목록")
          List<NewImage> newImages,
      @JsonSetter(nulls = Nulls.AS_EMPTY) @Schema(description = "삭제할 이미지 ID 목록")
          List<Long> deleted) {

    @Schema(description = "유지할 기존 이미지 정보")
    public record KeepImage(
        @NotNull @Schema(description = "유지할 이미지 ID", example = "1") Long id,
        @Min(1) @Schema(description = "이미지 순서", example = "1") int sequenceNumber) {}

    @Schema(description = "신규 추가할 이미지 정보")
    public record NewImage(
        @NotBlank
            @ValidImageUrl
            @Schema(description = "이미지 URL", example = "https://leafresh.io/example.jpg")
            String imageUrl,
        @NotNull @Schema(description = "이미지 타입", example = "GOOD") ExampleImageType type,
        @NotBlank @Schema(description = "이미지 설명", example = "올바른 인증 예시") String description,
        @Min(1) @Schema(description = "이미지 순서", example = "1") int sequenceNumber) {}
  }
}

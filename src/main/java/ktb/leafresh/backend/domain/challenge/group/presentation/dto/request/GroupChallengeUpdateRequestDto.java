package ktb.leafresh.backend.domain.challenge.group.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.validator.ValidGcsImageUrl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "단체 챌린지 수정 요청")
public record GroupChallengeUpdateRequestDto(
        @NotBlank
        @Schema(description = "제목") String title,

        @NotBlank
        @Schema(description = "설명") String description,

        @NotBlank
        @Schema(description = "카테고리 (ex. ZERO_WASTE)") String category,

        @Positive
        @Schema(description = "최대 인원 수") int maxParticipantCount,

        @NotBlank
        @ValidGcsImageUrl
        @Schema(description = "썸네일 이미지 URL") String thumbnailImageUrl,

        @NotNull
        @Schema(description = "시작일") LocalDate startDate,

        @NotNull
        @Schema(description = "종료일") LocalDate endDate,

        @NotNull
        @Schema(description = "인증 시작 시간") LocalTime verificationStartTime,

        @NotNull
        @Schema(description = "인증 종료 시간") LocalTime verificationEndTime,

        @NotNull
        @Valid
        @Schema(description = "인증 예시 이미지 목록") ExampleImages exampleImages
) {
    public record ExampleImages(
            @Size(max = 5)
            @Schema(description = "기존 유지할 이미지 ID와 순서 목록") List<KeepImage> keep,

            @Schema(description = "신규 추가할 이미지 목록") List<NewImage> newImages,

            @Schema(description = "삭제할 이미지 ID 목록") List<Long> deleted
    ) {
        public record KeepImage(
                @NotNull
                @Schema(description = "유지할 이미지 ID") Long id,

                @Min(1)
                @Schema(description = "이미지 순서") int sequenceNumber
        ) {}

        public record NewImage(
                @NotBlank
                @ValidGcsImageUrl
                @Schema(description = "이미지 URL") String imageUrl,

                @NotNull
                @Schema(description = "이미지 타입") ExampleImageType type,

                @NotBlank
                @Schema(description = "이미지 설명") String description,

                @Min(1)
                @Schema(description = "이미지 순서") int sequenceNumber
        ) {}
    }
}

package ktb.leafresh.backend.domain.challenge.group.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.validator.ValidGcsImageUrl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "단체 챌린지 생성 요청")
public record GroupChallengeCreateRequestDto(
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

        @Valid
        @NotNull(message = "챌린지에는 최소 한 개 이상의 성공 예시 이미지가 필요합니다.")
        @Size(max = 5)
        @Schema(description = "인증 예시 이미지 목록") List<ExampleImageRequestDto> exampleImages
) {
    public record ExampleImageRequestDto(
            @NotBlank @ValidGcsImageUrl String imageUrl,
            @NotNull ExampleImageType type,
            @NotBlank String description,
            @Min(1) int sequenceNumber
    ) {}
}

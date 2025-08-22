package ktb.leafresh.backend.domain.verification.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.global.validator.ValidImageUrl;

@Schema(description = "개인 챌린지 인증 제출 요청 DTO")
public record PersonalChallengeVerificationRequestDto(
    @NotBlank(message = "인증 이미지 URL은 필수입니다.")
        @ValidImageUrl
        @Schema(description = "인증 이미지 URL", example = "https://example.com/verification-image.jpg")
        String imageUrl,
    @NotBlank(message = "인증 내용은 필수입니다.")
        @Schema(description = "인증 내용 설명", example = "개인 운동 목표 달성! 오늘도 열심히 운동했습니다.")
        String content) {}

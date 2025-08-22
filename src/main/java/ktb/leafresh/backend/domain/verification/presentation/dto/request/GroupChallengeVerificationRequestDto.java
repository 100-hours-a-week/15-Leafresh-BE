package ktb.leafresh.backend.domain.verification.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.global.validator.ValidImageUrl;

@Schema(description = "단체 챌린지 인증 제출 요청 DTO")
public record GroupChallengeVerificationRequestDto(
    @NotBlank(message = "인증 이미지 URL은 필수입니다.")
        @ValidImageUrl
        @Schema(description = "인증 이미지 URL", example = "https://example.com/verification-image.jpg")
        String imageUrl,
    @NotBlank(message = "인증 내용은 필수입니다.")
        @Schema(description = "인증 내용 설명", example = "오늘의 운동 인증입니다! 30분간 열심히 했어요.")
        String content) {}

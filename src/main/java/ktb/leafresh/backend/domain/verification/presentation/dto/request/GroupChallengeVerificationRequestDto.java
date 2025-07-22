package ktb.leafresh.backend.domain.verification.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.global.validator.ValidImageUrl;

@Schema(description = "단체 챌린지 인증 제출 요청 DTO")
public record GroupChallengeVerificationRequestDto(

        @NotBlank(message = "인증 이미지 URL은 필수입니다.")
        @ValidImageUrl
        @Schema(description = "이미지 URL") String imageUrl,

        @NotBlank(message = "인증 내용은 필수입니다.")
        @Schema(description = "내용") String content
) {}

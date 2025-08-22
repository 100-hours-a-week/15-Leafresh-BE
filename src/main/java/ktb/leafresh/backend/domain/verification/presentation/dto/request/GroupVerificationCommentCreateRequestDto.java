package ktb.leafresh.backend.domain.verification.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "단체 인증 댓글 작성 요청 DTO")
public record GroupVerificationCommentCreateRequestDto(
    @NotBlank(message = "내용은 필수 항목입니다.") @Schema(description = "댓글 내용", example = "정말 대단한 인증이네요!")
        String content) {}

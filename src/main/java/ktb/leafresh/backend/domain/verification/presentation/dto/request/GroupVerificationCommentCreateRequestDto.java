package ktb.leafresh.backend.domain.verification.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GroupVerificationCommentCreateRequestDto(
        @NotBlank(message = "댓글 내용은 필수 항목입니다.")
        String content
) {
}

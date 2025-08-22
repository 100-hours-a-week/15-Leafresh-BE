package ktb.leafresh.backend.domain.feedback.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AiFeedbackApiResponse", description = "외부 AI 서비스 API 전체 응답 래퍼")
public record AiFeedbackApiResponseDto(
    @Schema(
            description = "HTTP 상태 코드",
            example = "200",
            requiredMode = Schema.RequiredMode.REQUIRED)
        int status,
    @Schema(
            description = "응답 메시지",
            example = "피드백 생성 완료",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String message,
    @Schema(description = "실제 피드백 데이터", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        AiFeedbackResponseDto data) {}

package ktb.leafresh.backend.domain.chatbot.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "챗봇 자유 텍스트 요청 DTO")
public record ChatbotFreeTextRequestDto(
    @Schema(
            description = "세션 ID",
            example = "session123",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "sessionId는 필수입니다.")
        String sessionId,
    @Schema(
            description = "사용자 메시지",
            example = "환경 보호를 위한 챌린지를 추천해주세요",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "message는 필수입니다.")
        String message) {}

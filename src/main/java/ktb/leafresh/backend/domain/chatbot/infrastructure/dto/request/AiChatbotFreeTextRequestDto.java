package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 챗봇 자유 텍스트 요청 DTO")
public record AiChatbotFreeTextRequestDto(
    @Schema(description = "세션 ID", example = "session123") String sessionId,
    @Schema(description = "사용자 메시지", example = "환경 보호를 위한 챌린지를 추천해주세요") String message) {}

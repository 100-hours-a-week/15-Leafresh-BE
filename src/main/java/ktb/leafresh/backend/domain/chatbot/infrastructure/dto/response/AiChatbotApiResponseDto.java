package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 챗봇 API 응답 래퍼 DTO")
public record AiChatbotApiResponseDto(
    @Schema(description = "응답 상태 코드", example = "200") int status,
    @Schema(description = "응답 메시지", example = "성공") String message,
    @Schema(description = "AI 챗봇 응답 데이터") AiChatbotResponseDto data) {}

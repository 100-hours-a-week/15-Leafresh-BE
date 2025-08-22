package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 챗봇 기본 정보 요청 DTO")
public record AiChatbotBaseInfoRequestDto(
    @Schema(description = "세션 ID", example = "session123") String sessionId,
    @Schema(description = "지역 정보", example = "서울") String location,
    @Schema(description = "근무 형태", example = "사무직") String workType,
    @Schema(description = "카테고리", example = "환경") String category) {}

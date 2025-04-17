package ktb.leafresh.backend.domain.chatbot.presentation.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatbotFreeTextResponseDto(
        String recommend,
        List<ChallengeDto> challenges
) {
    @Builder
    public record ChallengeDto(
            String title,
            String description
    ) {}
}

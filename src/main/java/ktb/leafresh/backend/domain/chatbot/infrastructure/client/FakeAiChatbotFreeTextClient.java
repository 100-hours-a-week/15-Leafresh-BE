package ktb.leafresh.backend.domain.chatbot.infrastructure.client;

import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotResponseDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("local")
public class FakeAiChatbotFreeTextClient implements AiChatbotFreeTextClient {

    @Override
    public AiChatbotResponseDto getRecommendation(AiChatbotFreeTextRequestDto requestDto) {
        return new AiChatbotResponseDto(
                "제로웨이스트",
                List.of(
                        new AiChatbotResponseDto.Challenge("텀블러 사용하기", "일회용 컵 대신 텀블러 사용 실천하기", "ZERO_WASTE", "제로웨이스트"),
                        new AiChatbotResponseDto.Challenge("대중교통 이용하기", "자차 대신 버스나 지하철 이용", "ZERO_WASTE", "제로웨이스트")
                )
        );
    }
}

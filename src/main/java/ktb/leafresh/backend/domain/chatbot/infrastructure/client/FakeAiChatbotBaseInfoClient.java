package ktb.leafresh.backend.domain.chatbot.infrastructure.client;

import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotBaseInfoResponseDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("local")
public class FakeAiChatbotBaseInfoClient implements AiChatbotBaseInfoClient {
    @Override
    public AiChatbotBaseInfoResponseDto getRecommendation(AiChatbotBaseInfoRequestDto requestDto) {
        return new AiChatbotBaseInfoResponseDto(
                "제로웨이스트",
                List.of(
                        new AiChatbotBaseInfoResponseDto.Challenge("제로웨이스트 입문", "일상 속 쓰레기를 줄이는 챌린지"),
                        new AiChatbotBaseInfoResponseDto.Challenge("플라스틱 줄이기", "텀블러, 장바구니 실천하기")
                )
        );
    }
}

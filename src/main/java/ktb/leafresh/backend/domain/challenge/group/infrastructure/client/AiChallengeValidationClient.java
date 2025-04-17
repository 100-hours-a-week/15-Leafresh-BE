//package ktb.leafresh.backend.domain.challenge.group.infrastructure.client;
//
//import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.request.AiChallengeValidationRequestDto;
//import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.response.AiChallengeValidationResponseDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Component
//@RequiredArgsConstructor
//public class AiChallengeValidationClient {
//
//    private final WebClient webClient;
//
//    public AiChallengeValidationResponseDto validateChallenge(AiChallengeValidationRequestDto requestDto) {
//        return webClient.post()
//                .uri("/ai/challenges/group/validation")
//                .bodyValue(requestDto)
//                .retrieve()
//                .bodyToMono(AiChallengeValidationResponseDto.class)
//                .block(); // 동기식 호출. block()을 사용해 동기 방식으로 처리하고 있음. Reactive 체계와 혼용할 경우는 주의해야 함
//    }
//}

package ktb.leafresh.backend.domain.challenge.group.infrastructure.client;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.request.AiChallengeValidationRequestDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.response.AiChallengeValidationResponseDto;

public interface AiChallengeValidationClient {
    AiChallengeValidationResponseDto validateChallenge(AiChallengeValidationRequestDto requestDto);
}

package ktb.leafresh.backend.domain.challenge.group.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.request.AiChallengeValidationRequestDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.response.AiChallengeValidationApiResponseDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.response.AiChallengeValidationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@Profile("!local")
public class HttpAiChallengeValidationClient implements AiChallengeValidationClient {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final WebClient aiServerWebClient;

  public HttpAiChallengeValidationClient(
      @Qualifier("makeChallengeAiWebClient") WebClient aiServerWebClient) {
    this.aiServerWebClient = aiServerWebClient;
  }

  @Override
  public AiChallengeValidationResponseDto validateChallenge(
      AiChallengeValidationRequestDto requestDto) {
    log.info("[AI 검증 요청 시작] /ai/challenges/group/validation");

    try {
      // 요청 바디 직렬화 로그 출력
      String json = objectMapper.writeValueAsString(requestDto);
      log.info("[AI 요청 JSON] {}", json);
    } catch (Exception e) {
      log.error("요청 바디 직렬화 실패", e);
    }

    try {
      String rawJson =
          aiServerWebClient
              .post()
              .uri("/ai/challenges/group/validation")
              .bodyValue(requestDto)
              .retrieve()
              .bodyToMono(String.class)
              .block();

      log.info("[AI 응답 원문] {}", rawJson);

      ObjectMapper objectMapper = new ObjectMapper();
      AiChallengeValidationApiResponseDto parsed =
          objectMapper.readValue(rawJson, AiChallengeValidationApiResponseDto.class);

      return parsed.data();

    } catch (Exception e) {
      log.error("[AI 응답 파싱 실패] {}", e.getMessage(), e);
      throw new RuntimeException("AI 응답 파싱 실패", e);
    }
  }
}

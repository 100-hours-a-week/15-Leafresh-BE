package ktb.leafresh.backend.domain.feedback.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.AiFeedbackCreationRequestDto;
import ktb.leafresh.backend.domain.feedback.infrastructure.dto.response.AiFeedbackApiResponseDto;
import ktb.leafresh.backend.domain.feedback.infrastructure.dto.response.AiFeedbackResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.net.SocketTimeoutException;
import java.time.Duration;

@Slf4j
@Component
@Profile("!local")
public class HttpFeedbackCreationClient implements FeedbackCreationClient {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final WebClient aiServerWebClient;

  public HttpFeedbackCreationClient(@Qualifier("textAiWebClient") WebClient aiServerWebClient) {
    this.aiServerWebClient = aiServerWebClient;
  }

  @Override
  public void requestWeeklyFeedback(AiFeedbackCreationRequestDto requestDto) {
    try {
      log.info("[AI 피드백 생성 요청 시작]");
      log.debug("[요청 DTO] {}", requestDto);

      String rawJson =
          aiServerWebClient
              .post()
              .uri("/ai/feedback")
              .bodyValue(requestDto)
              .retrieve()
              .bodyToMono(String.class)
              .block(Duration.ofSeconds(5));

      log.info("[AI 응답 수신 완료]");
      log.debug("[AI 응답 원문 JSON] {}", rawJson);

      AiFeedbackApiResponseDto parsed =
          objectMapper.readValue(rawJson, AiFeedbackApiResponseDto.class);

      if (parsed.status() == 200) {
        AiFeedbackResponseDto result = parsed.data();
        if (result == null || result.content() == null || result.content().isBlank()) {
          log.warn("[AI 응답 오류] data.content 비어 있음");
          throw new CustomException(FeedbackErrorCode.FEEDBACK_SERVER_ERROR);
        }
        log.info("[AI 피드백 응답 파싱 완료] content={}", result.content());

      } else if (parsed.status() == 202) {
        log.info("[AI 피드백 요청 정상 접수 - 비동기 처리 예정]");
        // 202일 경우에는 content 비어 있어도 정상 처리로 간주

      } else {
        log.error("[AI 응답 에러] status={}, message={}", parsed.status(), parsed.message());
        throw new CustomException(FeedbackErrorCode.FEEDBACK_SERVER_ERROR);
      }

    } catch (WebClientRequestException ex) {
      Throwable cause = ex.getCause();
      if (cause instanceof SocketTimeoutException) {
        log.error("[AI 피드백 요청 타임아웃]", ex);
        throw new CustomException(FeedbackErrorCode.FEEDBACK_SERVER_ERROR);
      }
      log.error("[AI 서버 연결 실패]", ex);
      throw new CustomException(FeedbackErrorCode.FEEDBACK_SERVER_ERROR);
    } catch (Exception e) {
      log.error("[피드백 생성 요청 중 예외 발생]", e);
      throw new CustomException(FeedbackErrorCode.FEEDBACK_SERVER_ERROR);
    }
  }
}

package ktb.leafresh.backend.domain.feedback.infrastructure.client;

import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.FeedbackCreationRequestDto;
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

    private final WebClient aiServerWebClient;

    public HttpFeedbackCreationClient(@Qualifier("aiServerWebClient") WebClient aiServerWebClient) {
        this.aiServerWebClient = aiServerWebClient;
    }

    @Override
    public void requestWeeklyFeedback(FeedbackCreationRequestDto requestDto) {
        try {
            log.info("[AI 피드백 생성 요청 시작]");
            log.debug("[요청 DTO] {}", requestDto);

            String response = aiServerWebClient.post()
                    .uri("/ai/feedback")
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));

            log.info("[AI 피드백 요청 응답 수신 완료] raw: {}", response);
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

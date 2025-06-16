package ktb.leafresh.backend.domain.chatbot.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"docker-local", "docker-prod"})
public class ChatbotRecommendationSseService {

    @Qualifier("textAiWebClient")
    private final WebClient textAiWebClient;

    private final ObjectMapper objectMapper;

    public Flux<ServerSentEvent<String>> streamFlux(String aiPath, Object dto) {
        Map<String, String> queryParams = objectMapper.convertValue(dto, Map.class);

        return textAiWebClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(aiPath);
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .timeout(Duration.ofMinutes(5))
                .onErrorResume(e -> {
                    log.warn("[AI 응답 타임아웃 또는 오류로 SSE 종료]", e);
                    return Flux.just(
                            ServerSentEvent.<String>builder()
                                    .event("error")
                                    .data("AI 응답이 지연되어 연결이 종료되었습니다.")
                                    .build()
                    );
                })
                .map(event -> {
                    String eventName = event.event();
                    String data = event.data();
                    if (data == null) {
                        log.warn("[SSE 무효 이벤트] event: {}, data: null", eventName);
                        return null;
                    }

                    log.info("[SSE 이벤트 수신] event: {}, data: {}", eventName, data);

                    return ServerSentEvent.<String>builder()
                            .event(eventName == null ? "message" : eventName)
                            .data(data)
                            .build();
                })
                .filter(Objects::nonNull)
                .takeUntil(event -> {
                    boolean isClose = "close".equalsIgnoreCase(event.event());
                    if (isClose) {
                        log.info("[SSE 종료 신호 감지] event: close → Flux 종료 예정");
                    }
                    return isClose;
                });
    }
}

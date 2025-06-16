package ktb.leafresh.backend.domain.chatbot.application.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Slf4j
@Component
@Profile("docker-local")
public class ChatbotSseStreamHandlerImpl implements ChatbotSseStreamHandler {

    private final WebClient textAiWebClient;

    public ChatbotSseStreamHandlerImpl(@Qualifier("textAiWebClient") WebClient textAiWebClient) {
        this.textAiWebClient = textAiWebClient;
    }

    @Override
    public void streamToEmitter(SseEmitter emitter, String uriWithQueryParams) {
        Flux<ServerSentEvent<String>> eventStream = textAiWebClient.get()
                .uri(uriWithQueryParams)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {});

        eventStream.subscribe(
                event -> {
                    String rawEventName = event.event();
                    String eventName = (rawEventName == null || rawEventName.isBlank()) ? "message" : rawEventName;

                    String data = event.data();
                    if (data == null) {
                        log.warn("[SSE 무효 이벤트 수신] event: {}, data: null", eventName);
                        return;
                    }

                    try {
                        if ("close".equalsIgnoreCase(eventName)) {
                            log.info("[SSE 종료 이벤트 수신] event: close, data: {}", data);

                            try {
                                emitter.send(SseEmitter.event()
                                        .name("close")
                                        .data(data, MediaType.APPLICATION_JSON));
                            } catch (IOException e) {
                                log.warn("[SSE 종료 이벤트 전송 실패] {}", e.getMessage());
                            }

                            emitter.complete();
                            return;
                        }

                        if (data.isBlank()) {
                            log.debug("[SSE Keep-Alive 전송] event: {}, data: <빈 문자열>", eventName);
                            emitter.send(SseEmitter.event().comment("ping"));
                        } else {
                            log.info("[SSE 응답 전달] event: {}, data: {}", eventName, data);
                            emitter.send(SseEmitter.event()
                                    .name(eventName)
                                    .data(data, MediaType.APPLICATION_JSON));
                        }

                    } catch (IOException e) {
                        log.warn("[SSE 전송 실패] {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("[SSE 스트림 오류 발생]", error);
                    emitter.completeWithError(error);
                },
                () -> {
                    log.info("[SSE 스트림 종료 콜백 발생] → 무시됨 (event: close로만 종료)");
                }
        );
    }
}

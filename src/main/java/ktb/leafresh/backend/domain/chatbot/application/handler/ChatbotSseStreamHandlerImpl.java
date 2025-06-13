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
                    try {
                        emitter.send(SseEmitter.event()
                                .name(event.event())
                                .data(event.data()));
                    } catch (IOException e) {
                        log.warn("[SSE 전송 실패] {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("[SSE 스트림 오류]", error);
                    emitter.completeWithError(error);
                },
                () -> {
                    log.info("[SSE 스트림 종료]");
                    emitter.complete();
                }
        );
    }
}

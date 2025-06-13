package ktb.leafresh.backend.domain.chatbot.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatbotSseStreamHandlerImpl implements ChatbotSseStreamHandler {

    private final WebClient textAiWebClient;

    @Override
    public void streamToEmitter(SseEmitter emitter, String uriWithQueryParams) {
        try {
            Flux<ServerSentEvent<String>> eventStream = textAiWebClient.get()
                    .uri(uriWithQueryParams)
                    .retrieve()
                    .bodyToFlux((Class<ServerSentEvent<String>>)(Class<?>)ServerSentEvent.class);

            eventStream.subscribe(
                    event -> {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name(event.event())
                                    .data(event.data()));
                        } catch (IOException e) {
                            log.warn("[SSE 전송 실패] {}", e.toString());
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        log.warn("[AI SSE 스트림 오류] {}", error.toString());
                        emitter.completeWithError(error);
                    },
                    () -> {
                        log.info("[AI SSE 스트림 완료]");
                        emitter.complete();
                    }
            );
        } catch (Exception e) {
            log.error("[SSE 스트림 처리 중 예외]", e);
            emitter.completeWithError(e);
        }
    }
}

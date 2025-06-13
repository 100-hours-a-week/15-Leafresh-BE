//package ktb.leafresh.backend.domain.chatbot.application.handler;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Profile;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.MediaType;
//import org.springframework.http.codec.ServerSentEvent;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//import reactor.core.publisher.Flux;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//@Profile("!local")
//public class HttpChatbotSseStreamHandler implements ChatbotSseStreamHandler {
//
//    private final WebClient textAiWebClient;
//
//    public HttpChatbotSseStreamHandler(@Qualifier("textAiWebClient") WebClient textAiWebClient) {
//        this.textAiWebClient = textAiWebClient;
//    }
//
//    @Override
//    public void streamToEmitter(SseEmitter emitter, String uriWithQueryParams) {
//        Flux<ServerSentEvent<String>> flux = textAiWebClient.get()
//                .uri(uriWithQueryParams)
//                .accept(MediaType.TEXT_EVENT_STREAM)
//                .retrieve()
//                .bodyToFlux(new ParameterizedTypeReference<>() {});
//
//        flux.doOnNext(event -> {
//                    StringBuilder sb = new StringBuilder();
//                    if (event.event() != null) sb.append("event: ").append(event.event()).append("\n");
//                    if (event.data() != null) sb.append("data: ").append(event.data()).append("\n");
//                    sb.append("\n");
//
//                    try {
//                        emitter.send(sb.toString());
//                    } catch (IOException e) {
//                        log.warn("[SSE 전송 실패] {}", e.getMessage());
//                        emitter.completeWithError(e);
//                    }
//                })
//                .doOnComplete(emitter::complete)
//                .doOnError(e -> {
//                    log.error("[SSE 스트림 오류] {}", e.getMessage());
//                    emitter.completeWithError(e);
//                })
//                .subscribe();
//    }
//}

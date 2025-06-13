package ktb.leafresh.backend.domain.chatbot.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.chatbot.application.handler.ChatbotSseStreamHandler;
import ktb.leafresh.backend.global.util.sse.SseStreamExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotRecommendationSseService {

    @Value("${ai-server.text-base-url}")
    private String aiServerBaseUrl;
    private final ChatbotSseStreamHandler streamHandler;
    private final SseStreamExecutor sseStreamExecutor;
    private final ObjectMapper objectMapper;

    public SseEmitter stream(String aiUri, Object dto) {
        SseEmitter emitter = new SseEmitter(60_000L);

        emitter.onCompletion(() -> log.info("[SSE 완료]"));
        emitter.onTimeout(() -> {
            log.warn("[SSE 타임아웃]");
            emitter.complete();
        });
        emitter.onError(e -> {
            log.warn("[SSE 에러 발생]", e);
        });

        String uri = buildUriWithParams(aiUri, dto);

        sseStreamExecutor.execute(emitter, () ->
                streamHandler.streamToEmitter(emitter, uri)
        );

        return emitter;
    }

    private String buildUriWithParams(String aiUri, Object dto) {
        Map<String, String> map = objectMapper.convertValue(dto, Map.class);

        log.debug("[SSE 요청 파라미터] {}", map);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(aiServerBaseUrl + aiUri);

        map.forEach((key, value) -> {
            log.debug("[쿼리 파라미터 추가] {}={}", key, value);
            builder.queryParam(key, value);
        });

        String uri = builder.toUriString();

        log.info("[AI 요청 URI] {}", uri);

        return uri;
    }
}

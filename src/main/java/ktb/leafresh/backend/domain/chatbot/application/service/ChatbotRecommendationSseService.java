package ktb.leafresh.backend.domain.chatbot.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.chatbot.application.handler.ChatbotSseStreamHandler;
import ktb.leafresh.backend.global.util.sse.SseStreamExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotRecommendationSseService {

    private final ChatbotSseStreamHandler streamHandler;
    private final SseStreamExecutor sseStreamExecutor;
    private final ObjectMapper objectMapper;

    public SseEmitter stream(String aiUri, Object dto) {
        SseEmitter emitter = new SseEmitter(60_000L);
        String uri = buildUriWithParams(aiUri, dto);

        sseStreamExecutor.execute(emitter, () ->
                streamHandler.streamToEmitter(emitter, uri)
        );

        return emitter;
    }

    private String buildUriWithParams(String aiUri, Object dto) {
        Map<String, String> map = objectMapper.convertValue(dto, Map.class);
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(aiUri);
        map.forEach(builder::queryParam);
        return builder.toUriString();
    }
}

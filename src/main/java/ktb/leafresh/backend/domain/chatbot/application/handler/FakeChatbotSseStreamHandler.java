package ktb.leafresh.backend.domain.chatbot.application.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Component
@Profile("local")
public class FakeChatbotSseStreamHandler implements ChatbotSseStreamHandler {

    @Override
    public void streamToEmitter(SseEmitter emitter, String uriWithQueryParams) {
        try {
            emitter.send("event: challenge\ndata: {\"message\":\"fake1\"}\n\n");
            emitter.send("event: challenge\ndata: {\"message\":\"fake2\"}\n\n");
            emitter.send("event: close\ndata: {\"message\":\"모든 챌린지 추천 완료\"}\n\n");
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}

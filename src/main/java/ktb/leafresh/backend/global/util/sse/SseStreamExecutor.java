package ktb.leafresh.backend.global.util.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 비동기 실행기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseStreamExecutor {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void execute(SseEmitter emitter, Runnable task) {
        executor.execute(task);
    }
}

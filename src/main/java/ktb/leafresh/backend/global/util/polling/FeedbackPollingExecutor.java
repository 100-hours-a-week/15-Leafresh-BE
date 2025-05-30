package ktb.leafresh.backend.global.util.polling;

import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class FeedbackPollingExecutor {

    private static final int TIMEOUT_MS = 10000;
    private static final int INTERVAL_MS = 500;

    public FeedbackResponseDto poll(Supplier<FeedbackResponseDto> supplier) {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < TIMEOUT_MS) {
            FeedbackResponseDto result = supplier.get();

            if (result.getContent() != null) {
                return result;
            }

            try {
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return new FeedbackResponseDto(null);
    }
}

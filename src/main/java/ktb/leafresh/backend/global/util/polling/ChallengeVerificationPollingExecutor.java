package ktb.leafresh.backend.global.util.polling;

import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ChallengeVerificationPollingExecutor {

    private static final int TIMEOUT_MILLIS = 10000;
    private static final int CHECK_INTERVAL_MILLIS = 500;

    public ChallengeStatus poll(Supplier<ChallengeStatus> statusSupplier) {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < TIMEOUT_MILLIS) {
            ChallengeStatus status = statusSupplier.get();

            if (status != ChallengeStatus.PENDING_APPROVAL) {
                return status;
            }

            try {
                Thread.sleep(CHECK_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return ChallengeStatus.PENDING_APPROVAL;
    }
}

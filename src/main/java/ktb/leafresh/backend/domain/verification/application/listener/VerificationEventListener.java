package ktb.leafresh.backend.domain.verification.application.listener;

import ktb.leafresh.backend.domain.verification.domain.event.VerificationCreatedEvent;
import ktb.leafresh.backend.domain.verification.infrastructure.client.AiVerificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationEventListener {

    private final AiVerificationClient aiClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(VerificationCreatedEvent event) {
        log.info("[이벤트 리스너] 인증 정보 저장 커밋 완료. AI 서버로 전송 시작");
        aiClient.verifyImage(event.requestDto());
    }
}

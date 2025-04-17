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
        try {
            log.info("[이벤트 리스너] 인증 정보 저장 커밋 완료. AI 서버로 전송 시작");
            aiClient.verifyImage(event.requestDto());
            log.info("[이벤트 리스너] AI 요청 완료");
        } catch (Exception e) {
            log.error("[이벤트 리스너] AI 요청 중 예외 발생: {}", e.getMessage(), e);
            // → retry 메커니즘이 있다면 여기에 큐 또는 재시도 등록 로직 삽입
        }
    }
}

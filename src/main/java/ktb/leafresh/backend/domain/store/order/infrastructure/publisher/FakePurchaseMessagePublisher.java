package ktb.leafresh.backend.domain.store.order.infrastructure.publisher;

import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@Slf4j
public class FakePurchaseMessagePublisher implements PurchaseMessagePublisher {
    @Override
    public void publish(PurchaseCommand command) {
        log.info("[로컬용 가짜 PubSub 발행] {}", command);
        // 로컬에서는 로그만 찍거나, 테스트용 로컬 큐에 저장해도 됨
    }
}

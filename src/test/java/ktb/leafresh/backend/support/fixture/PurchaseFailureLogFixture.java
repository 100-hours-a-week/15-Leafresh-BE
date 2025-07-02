package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseFailureLog;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;

import java.time.LocalDateTime;

public class PurchaseFailureLogFixture {

    private static final LocalDateTime FIXED_OCCURRED_AT = LocalDateTime.of(2024, 1, 1, 12, 0);

    public static PurchaseFailureLog of(Member member, Product product) {
        return PurchaseFailureLog.builder()
                .member(member)
                .product(product)
                .reason("테스트용 실패 사유")
                .requestBody("{\"example\": true}")
                .occurredAt(FIXED_OCCURRED_AT)
                .build();
    }
}

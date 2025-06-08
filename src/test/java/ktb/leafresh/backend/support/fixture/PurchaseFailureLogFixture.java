package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseFailureLog;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;

import java.time.LocalDateTime;

public class PurchaseFailureLogFixture {

    public static PurchaseFailureLog of(Member member, Product product) {
        return PurchaseFailureLog.builder()
                .id(1L)
                .member(member)
                .product(product)
                .reason("테스트용 실패 사유")
                .requestBody("{\"example\": true}")
                .occurredAt(LocalDateTime.now())
                .build();
    }
}

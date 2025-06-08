package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseIdempotencyKey;

public class PurchaseIdempotencyKeyFixture {

    public static PurchaseIdempotencyKey of(Member member) {
        return PurchaseIdempotencyKey.builder()
                .id(1L)
                .member(member)
                .idempotencyKey("test-idempotency-key")
                .build();
    }
}

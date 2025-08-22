package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseIdempotencyKey;

public class PurchaseIdempotencyKeyFixture {

  public static PurchaseIdempotencyKey of(Member member) {
    return new PurchaseIdempotencyKey(member, "test-idempotency-key");
  }
}

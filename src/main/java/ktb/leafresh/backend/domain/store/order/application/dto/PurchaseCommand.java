package ktb.leafresh.backend.domain.store.order.application.dto;

import java.time.LocalDateTime;

public record PurchaseCommand(
    Long memberId,
    Long productId,
    Long timedealPolicyId,
    Integer quantity,
    String idempotencyKey,
    LocalDateTime requestedAt) {}

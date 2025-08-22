package ktb.leafresh.backend.domain.store.product.application.event;

public record ProductUpdatedEvent(Long productId, boolean isTimeDeal) {}

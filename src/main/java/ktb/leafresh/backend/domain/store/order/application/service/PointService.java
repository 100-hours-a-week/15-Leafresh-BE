package ktb.leafresh.backend.domain.store.order.application.service;

public interface PointService {
  boolean hasEnoughPoints(Long memberId, int amount);
}

package ktb.leafresh.backend.domain.store.product.application.service;

import jakarta.transaction.Transactional;
import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.TimedealUpdateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.TimedealErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimedealUpdateService {

  private final TimedealPolicyRepository timedealPolicyRepository;
  private final ProductCacheLockFacade productCacheLockFacade;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void update(Long dealId, TimedealUpdateRequestDto dto) {
    log.info("타임딜 수정 요청 - dealId={}, start={}, end={}", dealId, dto.startTime(), dto.endTime());

    TimedealPolicy policy =
        timedealPolicyRepository
            .findById(dealId)
            .orElseThrow(() -> new CustomException(TimedealErrorCode.PRODUCT_NOT_FOUND));

    boolean shouldUpdateStockCache = false;
    boolean isStockChanged = false;
    boolean isTimeChanged = false;

    int originalStock = policy.getStock();
    LocalDateTime originalStart = policy.getStartTime();
    LocalDateTime originalEnd = policy.getEndTime();

    // 시간 유효성 검사
    if (dto.startTime() != null && dto.endTime() != null) {
      if (dto.startTime().isAfter(dto.endTime())) {
        throw new CustomException(TimedealErrorCode.INVALID_TIME);
      }

      boolean hasOverlap =
          timedealPolicyRepository.existsByProductIdAndTimeOverlapExceptSelf(
              policy.getProduct().getId(),
              dto.startTime().toLocalDateTime(),
              dto.endTime().toLocalDateTime(),
              dealId);
      if (hasOverlap) throw new CustomException(TimedealErrorCode.OVERLAPPING_TIME);

      if (!dto.startTime().equals(originalStart) || !dto.endTime().equals(originalEnd)) {
        policy.updateTime(dto.startTime().toLocalDateTime(), dto.endTime().toLocalDateTime());
        shouldUpdateStockCache = true;
        isTimeChanged = true;
      }
    }

    // 가격/퍼센트 유효성
    if (dto.discountedPrice() != null && dto.discountedPrice() < 1) {
      throw new CustomException(TimedealErrorCode.INVALID_PRICE);
    }
    if (dto.discountedPercentage() != null && dto.discountedPercentage() < 1) {
      throw new CustomException(TimedealErrorCode.INVALID_PERCENT);
    }

    if (dto.discountedPrice() != null || dto.discountedPercentage() != null) {
      policy.updatePriceAndPercent(dto.discountedPrice(), dto.discountedPercentage());
    }

    // 재고 유효성 및 업데이트
    if (dto.stock() == null || dto.stock() < 0) {
      throw new CustomException(TimedealErrorCode.INVALID_STOCK);
    }
    if (!dto.stock().equals(originalStock)) {
      policy.updateStock(dto.stock());
      shouldUpdateStockCache = true;
      isStockChanged = true;
    }

    // 캐시 갱신
    if (shouldUpdateStockCache) {
      productCacheLockFacade.cacheTimedealStock(
          policy.getId(), policy.getStock(), policy.getEndTime());
      String reason = isStockChanged ? "재고 변경" : isTimeChanged ? "시간 변경" : "기타";
      log.info("[TimedealUpdateService] 타임딜 캐시 갱신 - policyId={}, 이유: {}", policy.getId(), reason);
    }

    // 단건 캐시 + ZSet + 목록 무효화
    productCacheLockFacade.evictTimedealCache(policy);
    productCacheLockFacade.updateSingleTimedealCache(policy);

    // 상품 전체 갱신 이벤트
    eventPublisher.publishEvent(new ProductUpdatedEvent(policy.getProduct().getId(), true));

    log.info("타임딜 수정 완료 - id={}", policy.getId());
  }
}

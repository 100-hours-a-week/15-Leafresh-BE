package ktb.leafresh.backend.domain.store.product.application.service;

import jakarta.transaction.Transactional;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TimedealUpdateService {

    private final TimedealPolicyRepository timedealPolicyRepository;
    private final ProductCacheService productCacheService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void update(Long dealId, TimedealUpdateRequestDto dto) {
        log.info("타임딜 수정 요청 - dealId={}, start={}, end={}", dealId, dto.startTime(), dto.endTime());

        TimedealPolicy policy = timedealPolicyRepository.findById(dealId)
                .orElseThrow(() -> new CustomException(TimedealErrorCode.PRODUCT_NOT_FOUND));

        if (dto.startTime() != null && dto.endTime() != null && dto.startTime().isAfter(dto.endTime())) {
            throw new CustomException(TimedealErrorCode.INVALID_TIME);
        }

        if (dto.startTime() != null && dto.endTime() != null) {
            boolean hasOverlap = timedealPolicyRepository.existsByProductIdAndTimeOverlapExceptSelf(
                    policy.getProduct().getId(), dto.startTime(), dto.endTime(), dealId);
            if (hasOverlap) throw new CustomException(TimedealErrorCode.OVERLAPPING_TIME);
            policy.updateTime(dto.startTime(), dto.endTime());

            log.info("[TimedealUpdateService] 타임딜 재고 캐시 시도 - policyId={}, stock={}, endTime={}",
                    policy.getId(), policy.getStock(), dto.endTime());
            productCacheService.cacheTimedealStock(policy.getId(), policy.getStock(), dto.endTime());
            log.info("[TimedealUpdateService] 타임딜 재고 캐시 완료 - policyId={}", policy.getId());
        }

        if (dto.discountedPrice() != null && dto.discountedPrice() < 1) {
            throw new CustomException(TimedealErrorCode.INVALID_PRICE);
        }
        if (dto.discountedPercentage() != null && dto.discountedPercentage() < 1) {
            throw new CustomException(TimedealErrorCode.INVALID_PERCENT);
        }

        policy.updatePriceAndPercent(dto.discountedPrice(), dto.discountedPercentage());

        productCacheService.evictTimedealCache(policy);
        productCacheService.updateSingleTimedealCache(policy);
        eventPublisher.publishEvent(new ProductUpdatedEvent(policy.getProduct().getId(), true));

        try {
            log.info("타임딜 수정 완료 - id={}", policy.getId());
        } catch (Exception e) {
            log.error("타임딜 수정 실패", e);
            throw new CustomException(TimedealErrorCode.TIMEDEAL_SAVE_FAIL);
        }
    }
}

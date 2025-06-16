package ktb.leafresh.backend.domain.store.product.application.service;

import jakarta.transaction.Transactional;
import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.TimedealCreateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealCreateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.TimedealErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimedealCreateService {
    private final ProductRepository productRepository;
    private final TimedealPolicyRepository timedealPolicyRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ProductCacheService productCacheService;

    @Transactional
    public TimedealCreateResponseDto create(TimedealCreateRequestDto dto) {
        log.info("타임딜 등록 요청 - productId={}, start={}, end={}", dto.productId(), dto.startTime(), dto.endTime());

        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new CustomException(TimedealErrorCode.PRODUCT_NOT_FOUND));

        if (dto.startTime().isAfter(dto.endTime())) {
            throw new CustomException(TimedealErrorCode.INVALID_TIME);
        }

        boolean hasOverlap = timedealPolicyRepository.existsByProductIdAndTimeOverlap(
                dto.productId(), dto.startTime().toLocalDateTime(), dto.endTime().toLocalDateTime()
        );

        if (hasOverlap) {
            throw new CustomException(TimedealErrorCode.OVERLAPPING_TIME);
        }

        TimedealPolicy policy = TimedealPolicy.builder()
                .product(product)
                .discountedPrice(dto.discountedPrice())
                .discountedPercentage(dto.discountedPercentage())
                .stock(product.getStock())
                .startTime(dto.startTime().toLocalDateTime())
                .endTime(dto.endTime().toLocalDateTime())
                .build();

        try {
            TimedealPolicy savedPolicy = timedealPolicyRepository.save(policy);

            log.info("[TimedealCreateService] 타임딜 재고 캐시 시도 - policyId={}, stock={}, endTime={}",
                    savedPolicy.getId(), savedPolicy.getStock(), savedPolicy.getEndTime());

            productCacheService.cacheTimedealStock(savedPolicy.getId(), savedPolicy.getStock(), savedPolicy.getEndTime());

            timedealPolicyRepository.save(policy);

            log.info("[TimedealCreateService] 타임딜 재고 캐시 완료 - policyId={}", policy.getId());

            Product updatedProduct = productRepository.findById(dto.productId())
                    .orElseThrow(() -> new CustomException(TimedealErrorCode.PRODUCT_NOT_FOUND));

            productCacheService.updateSingleTimedealCache(policy);
            eventPublisher.publishEvent(new ProductUpdatedEvent(updatedProduct.getId(), true));
            log.info("타임딜 저장 및 캐시 반영 완료 - productId={}", product.getId());

            return new TimedealCreateResponseDto(policy.getId());
        } catch (Exception e) {
            log.error("타임딜 저장 실패", e);
            throw new CustomException(TimedealErrorCode.TIMEDEAL_SAVE_FAIL);
        }
    }
}

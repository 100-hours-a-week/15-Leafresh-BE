package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.factory.ProductFactory;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductCreateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductCreateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProductCreateService {

    private final ProductRepository productRepository;
    private final ProductFactory productFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final ProductCacheService productCacheService;

    @Transactional
    public ProductCreateResponseDto createProduct(ProductCreateRequestDto dto) {
        try {
            log.info("일반 상품 생성 요청: {}", dto.name());
            Product product = productFactory.create(dto);
            productRepository.save(product);

            productCacheService.cacheProductStock(product.getId(), product.getStock());

            eventPublisher.publishEvent(new ProductUpdatedEvent(product.getId(), false));

            log.info("상품 생성 완료 - id={}", product.getId());
            return new ProductCreateResponseDto(product.getId());
        } catch (Exception e) {
            log.error("상품 생성 중 예외 발생", e);
            throw new CustomException(ProductErrorCode.PRODUCT_CREATE_FAILED);
        }
    }
}

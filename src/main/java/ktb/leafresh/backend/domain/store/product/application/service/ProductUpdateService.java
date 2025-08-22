package ktb.leafresh.backend.domain.store.product.application.service;

import jakarta.transaction.Transactional;
import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductUpdateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductUpdateService {

  private final ProductRepository productRepository;
  private final ProductCacheLockFacade productCacheLockFacade;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void update(Long productId, ProductUpdateRequestDto dto) {
    log.info("일반 상품 수정 요청 - productId={}, dto={}", productId, dto);

    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

    try {
      if (dto.name() != null) product.updateName(dto.name());
      if (dto.description() != null) product.updateDescription(dto.description());
      if (dto.imageUrl() != null) product.updateImageUrl(dto.imageUrl());
      if (dto.price() != null) {
        if (dto.price() < 1) throw new CustomException(ProductErrorCode.INVALID_PRICE);
        product.updatePrice(dto.price());
      }
      if (dto.stock() != null) {
        if (dto.stock() < 0) throw new CustomException(ProductErrorCode.INVALID_STOCK);
        product.updateStock(dto.stock());
        productCacheLockFacade.cacheProductStock(product.getId(), dto.stock());
      }
      if (dto.status() != null) {
        try {
          ProductStatus status = ProductStatus.valueOf(dto.status());
          product.updateStatus(status);
        } catch (IllegalArgumentException e) {
          throw new CustomException(ProductErrorCode.INVALID_STATUS);
        }
      }

      productCacheLockFacade.evictCacheByProduct(product);

      boolean hasActiveTimedeal = product.getActiveTimedealPolicy(LocalDateTime.now()).isPresent();
      eventPublisher.publishEvent(new ProductUpdatedEvent(productId, hasActiveTimedeal));

      log.info("일반 상품 수정 완료 - productId={}", productId);
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("일반 상품 수정 중 예외 발생", e);
      throw new CustomException(ProductErrorCode.PRODUCT_UPDATE_FAILED);
    }
  }
}

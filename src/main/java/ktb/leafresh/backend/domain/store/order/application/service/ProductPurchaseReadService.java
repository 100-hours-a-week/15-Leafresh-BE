package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.ProductPurchaseQueryRepository;
import ktb.leafresh.backend.domain.store.order.presentation.dto.response.ProductPurchaseListResponseDto;
import ktb.leafresh.backend.domain.store.order.presentation.dto.response.ProductPurchaseSummaryResponseDto;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductPurchaseReadService {

    private final ProductPurchaseQueryRepository productPurchaseQueryRepository;

    public ProductPurchaseListResponseDto getPurchases(Long memberId, String input, Long cursorId, String cursorTimestamp, int size) {
        List<ProductPurchase> purchases = productPurchaseQueryRepository.findByMemberWithCursorAndSearch(memberId, input, cursorId, cursorTimestamp, size);

        var result = CursorPaginationHelper.paginateWithTimestamp(
                purchases,
                size,
                ProductPurchaseSummaryResponseDto::from,
                ProductPurchaseSummaryResponseDto::id,
                ProductPurchaseSummaryResponseDto::purchasedAt
        );

        return ProductPurchaseListResponseDto.from(result);
    }
}

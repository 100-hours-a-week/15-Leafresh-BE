package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductSearchQueryRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductListResponseDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductSummaryResponseDto;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchReadService {

    private final ProductSearchQueryRepository productSearchQueryRepository;

    public ProductListResponseDto search(String input, Long cursorId, String cursorTimestamp, int size) {
        List<Product> products = productSearchQueryRepository.findWithFilter(input, cursorId, cursorTimestamp, size);

        var result = CursorPaginationHelper.paginateWithTimestamp(
                products,
                size,
                ProductSummaryResponseDto::from,
                ProductSummaryResponseDto::getId,
                ProductSummaryResponseDto::getCreatedAt
        );

        return ProductListResponseDto.from(result);
    }
}

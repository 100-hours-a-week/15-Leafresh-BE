package ktb.leafresh.backend.domain.store.product.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "타임딜 상품 목록 응답")
public record TimedealProductListResponseDto(
    @Schema(description = "타임딜 상품 목록") List<TimedealProductSummaryResponseDto> timeDeals) {}

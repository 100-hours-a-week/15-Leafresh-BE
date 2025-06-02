package ktb.leafresh.backend.domain.store.product.infrastructure.repository;

import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductSummaryResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface TimedealProductQueryRepository {
    List<TimedealProductSummaryResponseDto> findTimedeals(LocalDateTime now, LocalDateTime oneWeekLater);

    List<TimedealProductSummaryResponseDto> findByIds(List<Long> ids);
}

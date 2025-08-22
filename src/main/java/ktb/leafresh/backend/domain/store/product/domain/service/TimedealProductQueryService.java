package ktb.leafresh.backend.domain.store.product.domain.service;

import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealProductQueryRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimedealProductQueryService {

  private final TimedealProductQueryRepository timedealProductQueryRepository;

  public List<TimedealProductSummaryResponseDto> findUpcomingOrOngoingWithinWeek() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneWeekLater = now.plusWeeks(1);

    return timedealProductQueryRepository.findTimedeals(now, oneWeekLater);
  }

  public List<TimedealProductSummaryResponseDto> findAllById(List<Long> policyIds) {
    return timedealProductQueryRepository.findByIds(policyIds);
  }
}

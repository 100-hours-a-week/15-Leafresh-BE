package ktb.leafresh.backend.domain.store.product.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.domain.service.TimedealProductQueryService;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheKeys;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductListResponseDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductSummaryResponseDto;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import ktb.leafresh.backend.support.fixture.TimedealPolicyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimedealProductReadServiceTest {

  @Mock private RedisTemplate<String, Object> redisTemplate;

  @Mock private TimedealProductQueryService queryService;

  @Mock private ValueOperations<String, Object> valueOps;

  @Mock private ZSetOperations<String, Object> zSetOps;

  @Mock private ObjectMapper objectMapper;

  @InjectMocks private TimedealProductReadService service;

  @BeforeEach
  void setup() {
    when(redisTemplate.opsForValue()).thenReturn(valueOps);
  }

  @Test
  @DisplayName("캐시 HIT 시 목록 반환")
  void findTimedealProducts_cacheHit() {
    // given
    TimedealProductSummaryResponseDto dto =
        toDto(TimedealPolicyFixture.createOngoingTimedeal(ProductFixture.createDefaultProduct()));
    TimedealProductListResponseDto cached = new TimedealProductListResponseDto(List.of(dto));
    when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(cached);
    when(objectMapper.convertValue(cached, TimedealProductListResponseDto.class))
        .thenReturn(cached);

    // when
    TimedealProductListResponseDto result = service.findTimedealProducts();

    // then
    assertThat(result.timeDeals()).hasSize(1);
    assertThat(result.timeDeals().get(0).title()).isEqualTo(dto.title());
    verify(redisTemplate, never()).opsForZSet();
  }

  @Test
  @DisplayName("캐시 MISS + 단건 HIT 시 목록 반환")
  void findTimedealProducts_cacheMiss_singleHit() {
    // given
    when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

    TimedealProductSummaryResponseDto dto =
        toDto(TimedealPolicyFixture.createOngoingTimedeal(ProductFixture.createDefaultProduct()));

    when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(null);
    when(zSetOps.rangeByScore(anyString(), anyDouble(), anyDouble()))
        .thenReturn(Set.of(dto.dealId()));
    when(valueOps.get(ProductCacheKeys.timedealSingle(dto.dealId()))).thenReturn(dto);
    when(objectMapper.convertValue(dto, TimedealProductSummaryResponseDto.class)).thenReturn(dto);

    // when
    TimedealProductListResponseDto result = service.findTimedealProducts();

    // then
    assertThat(result.timeDeals()).hasSize(1);
    assertThat(result.timeDeals().get(0).dealId()).isEqualTo(dto.dealId());
  }

  @Test
  @DisplayName("단건 MISS 시 Fallback 처리 성공")
  void findTimedealProducts_fallback_success() {
    // given
    when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

    TimedealProductSummaryResponseDto fallbackDto =
        toDto(TimedealPolicyFixture.createOngoingTimedeal(ProductFixture.createDefaultProduct()));

    when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(null);
    when(zSetOps.rangeByScore(anyString(), anyDouble(), anyDouble()))
        .thenReturn(Set.of(fallbackDto.dealId()));
    when(valueOps.get(ProductCacheKeys.timedealSingle(fallbackDto.dealId()))).thenReturn(null);
    when(queryService.findAllById(List.of(fallbackDto.dealId()))).thenReturn(List.of(fallbackDto));

    // when
    TimedealProductListResponseDto result = service.findTimedealProducts();

    // then
    assertThat(result.timeDeals()).hasSize(1);
    assertThat(result.timeDeals().get(0).title()).isEqualTo(fallbackDto.title());
    verify(queryService).findAllById(List.of(fallbackDto.dealId()));
  }

  @Test
  @DisplayName("ZSET 캐시 없음 시 빈 목록 반환")
  void findTimedealProducts_emptyZset() {
    // given
    when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

    when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(null);
    when(zSetOps.rangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(Set.of());
    when(queryService.findUpcomingOrOngoingWithinWeek()).thenReturn(List.of());

    // when
    TimedealProductListResponseDto result = service.findTimedealProducts();

    // then
    assertThat(result.timeDeals()).isEmpty();
  }

  private TimedealProductSummaryResponseDto toDto(TimedealPolicy policy) {
    if (policy == null) throw new IllegalArgumentException("policy is null");
    if (policy.getProduct() == null) throw new IllegalArgumentException("product is null");

    return new TimedealProductSummaryResponseDto(
        1L,
        1L,
        policy.getProduct().getName(),
        policy.getProduct().getDescription(),
        policy.getProduct().getPrice(),
        policy.getDiscountedPrice(),
        policy.getDiscountedPercentage(),
        policy.getStock(),
        policy.getProduct().getImageUrl(),
        policy.getStartTime().atOffset(ZoneOffset.UTC),
        policy.getEndTime().atOffset(ZoneOffset.UTC),
        String.valueOf(policy.getProduct().getStatus()),
        "ONGOING");
  }
}

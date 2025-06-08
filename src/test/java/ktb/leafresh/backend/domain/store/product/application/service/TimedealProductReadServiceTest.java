package ktb.leafresh.backend.domain.store.product.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ktb.leafresh.backend.domain.store.product.domain.service.TimedealProductQueryService;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheKeys;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductListResponseDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductSummaryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TimedealProductReadServiceTest {

    private RedisTemplate<String, Object> redisTemplate;
    private TimedealProductQueryService queryService;
    private ObjectMapper objectMapper;
    private TimedealProductReadService service;

    private ValueOperations<String, Object> valueOps;
    private ZSetOperations<String, Object> zSetOps;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        queryService = mock(TimedealProductQueryService.class);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        valueOps = mock(ValueOperations.class);
        zSetOps = mock(ZSetOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        service = new TimedealProductReadService(redisTemplate, queryService, objectMapper);
    }

    @Test
    @DisplayName("캐시 HIT 시 목록 반환")
    void findTimedealProducts_cacheHit() {
        var cached = new TimedealProductListResponseDto(List.of(
                new TimedealProductSummaryResponseDto(
                        1L, 1L, "비누", "설명",
                        3000, 2000, 30, 10,
                        "https://img.png",
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusHours(2),
                        "ACTIVE", "ONGOING"
                )
        ));

        when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(cached);

        TimedealProductListResponseDto result = service.findTimedealProducts();

        assertThat(result.timeDeals()).hasSize(1);
        verify(redisTemplate, never()).opsForZSet();
    }

    @Test
    @DisplayName("캐시 MISS + 단건 HIT 시 목록 반환")
    void findTimedealProducts_cacheMiss_singleHit() {
        when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(null);
        when(zSetOps.rangeByScore(any(), anyDouble(), anyDouble())).thenReturn(Set.of(1L));

        TimedealProductSummaryResponseDto dto = new TimedealProductSummaryResponseDto(
                1L, 1L, "샴푸", "세정력 좋은 샴푸",
                5000, 3500, 30, 20,
                "https://img.png",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                "ACTIVE", "ONGOING"
        );
        when(valueOps.get(ProductCacheKeys.timedealSingle(1L))).thenReturn(dto);

        TimedealProductListResponseDto result = service.findTimedealProducts();

        assertThat(result.timeDeals()).hasSize(1);
        assertThat(result.timeDeals().get(0).dealId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("단건 MISS 시 Fallback 처리 성공")
    void findTimedealProducts_fallback_success() {
        when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(null);
        when(zSetOps.rangeByScore(any(), anyDouble(), anyDouble())).thenReturn(Set.of(1L));
        when(valueOps.get(ProductCacheKeys.timedealSingle(1L))).thenReturn(null);

        TimedealProductSummaryResponseDto fallbackDto = new TimedealProductSummaryResponseDto(
                1L, 1L, "친환경 수세미", "지속 가능한 수세미",
                3500, 2000, 40, 10,
                "https://img.png",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                "ACTIVE", "ONGOING"
        );
        when(queryService.findAllById(List.of(1L))).thenReturn(List.of(fallbackDto));

        TimedealProductListResponseDto result = service.findTimedealProducts();

        assertThat(result.timeDeals()).hasSize(1);
        assertThat(result.timeDeals().get(0).dealId()).isEqualTo(1L);
        verify(queryService).findAllById(List.of(1L));
    }

    @Test
    @DisplayName("ZSET 캐시 없음 시 빈 목록 반환")
    void findTimedealProducts_emptyZset() {
        when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(null);
        when(zSetOps.rangeByScore(any(), anyDouble(), anyDouble())).thenReturn(Set.of());

        TimedealProductListResponseDto result = service.findTimedealProducts();

        assertThat(result.timeDeals()).isEmpty();
    }
}

//package ktb.leafresh.backend.domain.store.product.application.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
//import ktb.leafresh.backend.domain.store.product.domain.service.TimedealProductQueryService;
//import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheKeys;
//import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductListResponseDto;
//import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductSummaryResponseDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.data.redis.core.ZSetOperations;
//
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.List;
//import java.util.Set;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TimedealProductReadServiceTest {
//
//    @Mock
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Mock
//    private TimedealProductQueryService queryService;
//
//    @Mock
//    private ValueOperations<String, Object> valueOps;
//
//    @Mock
//    private ZSetOperations<String, Object> zSetOps;
//
//    @Mock
//    private ObjectMapper objectMapper;
//
//    @InjectMocks
//    private TimedealProductReadService service;
//
//    private static final OffsetDateTime FIXED_NOW = OffsetDateTime.of(2025, 7, 1, 12, 0, 0, 0, ZoneOffset.UTC);
//    private static final OffsetDateTime FIXED_START = FIXED_NOW.minusMinutes(10);
//    private static final OffsetDateTime FIXED_END = FIXED_NOW.plusHours(1);
//
//    @BeforeEach
//    void setUp() {
//        when(redisTemplate.opsForValue()).thenReturn(valueOps);
//    }
//
//    @Test
//    @DisplayName("캐시 HIT 시 목록 반환")
//    void findTimedealProducts_cacheHit() {
//        // given
//        TimedealProductSummaryResponseDto dto = createDto(1L, "비누", "ONGOING");
//        var cached = new TimedealProductListResponseDto(List.of(dto));
//
//        when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(cached);
//        when(objectMapper.convertValue(cached, TimedealProductListResponseDto.class)).thenReturn(cached);
//
//        // when
//        TimedealProductListResponseDto result = service.findTimedealProducts();
//
//        // then
//        assertThat(result.timeDeals()).hasSize(1);
//        assertThat(result.timeDeals().get(0)).usingRecursiveComparison().isEqualTo(dto);
//    }
//
//    @Test
//    @DisplayName("캐시 MISS + 단건 HIT 시 목록 반환")
//    void findTimedealProducts_cacheMiss_singleHit() {
//        // given
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
//        TimedealProductSummaryResponseDto dto = createDto(2L, "샴푸", "ONGOING");
//
//        when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(null);
//        when(zSetOps.rangeByScore(any(), anyDouble(), anyDouble())).thenReturn(Set.of(2L));
//        when(valueOps.get(ProductCacheKeys.timedealSingle(2L))).thenReturn(dto);
//        when(objectMapper.convertValue(dto, TimedealProductSummaryResponseDto.class)).thenReturn(dto);
//
//        // when
//        TimedealProductListResponseDto result = service.findTimedealProducts();
//
//        // then
//        assertThat(result.timeDeals()).hasSize(1);
//        assertThat(result.timeDeals().get(0)).usingRecursiveComparison().isEqualTo(dto);
//    }
//
//    @Test
//    @DisplayName("단건 MISS 시 Fallback 처리 성공")
//    void findTimedealProducts_fallback_success() {
//        // given
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
//        TimedealProductSummaryResponseDto fallbackDto = createDto(3L, "수세미", "ONGOING");
//
//        when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(null);
//        when(zSetOps.rangeByScore(any(), anyDouble(), anyDouble())).thenReturn(Set.of(3L));
//        when(valueOps.get(ProductCacheKeys.timedealSingle(3L))).thenReturn(null);
//        when(queryService.findAllById(List.of(3L))).thenReturn(List.of(fallbackDto));
//
//        // when
//        TimedealProductListResponseDto result = service.findTimedealProducts();
//
//        // then
//        assertThat(result.timeDeals()).hasSize(1);
//        assertThat(result.timeDeals().get(0)).usingRecursiveComparison().isEqualTo(fallbackDto);
//        verify(queryService).findAllById(List.of(3L));
//    }
//
//    @Test
//    @DisplayName("ZSET 캐시 없음 시 빈 목록 반환")
//    void findTimedealProducts_emptyZset() {
//        // given
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
//        when(valueOps.get(ProductCacheKeys.TIMEDEAL_LIST)).thenReturn(null);
//        when(zSetOps.rangeByScore(any(), anyDouble(), anyDouble())).thenReturn(Set.of());
//
//        // when
//        TimedealProductListResponseDto result = service.findTimedealProducts();
//
//        // then
//        assertThat(result.timeDeals()).isEmpty();
//    }
//
//    private TimedealProductSummaryResponseDto createDto(Long dealId, String title, String timeDealStatus) {
//        return new TimedealProductSummaryResponseDto(
//                dealId,
//                dealId + 10,
//                title,
//                "설명",
//                3000,
//                2000,
//                33,
//                15,
//                "https://img.png",
//                FIXED_START,
//                FIXED_END,
//                ProductStatus.ACTIVE.name(),
//                timeDealStatus
//        );
//    }
//}

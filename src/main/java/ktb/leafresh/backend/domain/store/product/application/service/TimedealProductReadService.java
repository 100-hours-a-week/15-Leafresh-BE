package ktb.leafresh.backend.domain.store.product.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.store.product.domain.service.TimedealProductQueryService;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheKeys;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductListResponseDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimedealProductReadService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TimedealProductQueryService timedealProductQueryService;
    private final ObjectMapper objectMapper;

    public TimedealProductListResponseDto findTimedealProducts() {
        log.info("[TimedealProductReadService] 타임딜 목록 조회 요청");

        // 1. 목록 캐시 확인
        Object cachedList = redisTemplate.opsForValue().get(ProductCacheKeys.TIMEDEAL_LIST);
        List<TimedealProductSummaryResponseDto> result;
        if (cachedList != null) {
            log.info("[TimedealProductReadService] 목록 캐시 HIT - key={}", ProductCacheKeys.TIMEDEAL_LIST);
            TimedealProductListResponseDto cachedDto = objectMapper.convertValue(cachedList, TimedealProductListResponseDto.class);
            result = cachedDto.timeDeals();
        } else {
            log.info("[TimedealProductReadService] 목록 캐시 MISS - key={}", ProductCacheKeys.TIMEDEAL_LIST);

            // 2. ZSET 조회
            long now = System.currentTimeMillis();
            long oneWeekLater = LocalDateTime.now().plusDays(7)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long twoHoursBeforeNow = LocalDateTime.now().minusHours(2)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            Set<Object> dealIds = redisTemplate.opsForZSet()
                    .rangeByScore(ProductCacheKeys.TIMEDEAL_ZSET, twoHoursBeforeNow, oneWeekLater);

            result = new ArrayList<>();
            if (dealIds != null && !dealIds.isEmpty()) {
                List<Long> missedPolicyIds = new ArrayList<>();

                for (Object policyIdObj : dealIds) {
                    Long policyId = Long.valueOf(policyIdObj.toString());
                    String key = ProductCacheKeys.timedealSingle(policyId);
                    Object cachedDto = redisTemplate.opsForValue().get(key);

                    if (cachedDto != null) {
                        result.add(objectMapper.convertValue(cachedDto, TimedealProductSummaryResponseDto.class));
                        log.info("[TimedealProductReadService] 단건 캐시 HIT - key={}", key);
                    } else {
                        missedPolicyIds.add(policyId);
                        log.warn("[TimedealProductReadService] 단건 캐시 MISS - key={}", key);
                    }
                }

                // 3. Fallback - 벌크 조회 + 병렬 캐싱
                if (!missedPolicyIds.isEmpty()) {
                    log.info("[TimedealProductReadService] DB fallback 시작 - size={}", missedPolicyIds.size());

                    List<TimedealProductSummaryResponseDto> fallbackList =
                            timedealProductQueryService.findAllById(missedPolicyIds);

                    fallbackList.parallelStream().forEach(dto -> {
                        String fallbackKey = ProductCacheKeys.timedealSingle(dto.dealId());
                        long ttl = Duration.between(LocalDateTime.now(), dto.dealEndTime()).toSeconds() + 60;
                        redisTemplate.opsForValue().set(fallbackKey, dto, Duration.ofSeconds(ttl));
                        log.info("[TimedealProductReadService] DB fallback 캐시 저장 - key={}, TTL={}초", fallbackKey, ttl);
                    });

                    result.addAll(fallbackList);
                }
            }

            // 4. 목록 캐시 저장
            TimedealProductListResponseDto responseDto = new TimedealProductListResponseDto(result);
            redisTemplate.opsForValue().set(
                    ProductCacheKeys.TIMEDEAL_LIST,
                    responseDto,
                    Duration.ofSeconds(60)
            );
            log.info("[TimedealProductReadService] 목록 캐시 저장 - key={}, TTL=60초", ProductCacheKeys.TIMEDEAL_LIST);
        }

        // 5. 상태값 동적 갱신
        LocalDateTime nowTime = LocalDateTime.now();
        List<TimedealProductSummaryResponseDto> updated = result.stream()
                .map(dto -> new TimedealProductSummaryResponseDto(
                        dto.dealId(),
                        dto.productId(),
                        dto.title(),
                        dto.description(),
                        dto.defaultPrice(),
                        dto.discountedPrice(),
                        dto.discountedPercentage(),
                        dto.stock(),
                        dto.imageUrl(),
                        dto.dealStartTime(),
                        dto.dealEndTime(),
                        dto.productStatus(),
                        determineTimeDealStatus(nowTime, dto.dealStartTime(), dto.dealEndTime())
                ))
                .toList();

        return new TimedealProductListResponseDto(updated);
    }

    private String determineTimeDealStatus(LocalDateTime now, LocalDateTime start, LocalDateTime end) {
        return (start.isBefore(now) && end.isAfter(now)) ? "ONGOING" : "UPCOMING";
    }
}

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

import java.time.*;
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

    // 1. 목록 캐시 조회
    Object cachedList = redisTemplate.opsForValue().get(ProductCacheKeys.TIMEDEAL_LIST);
    if (cachedList != null) {
      log.info("[TimedealProductReadService] 목록 캐시 HIT - key={}", ProductCacheKeys.TIMEDEAL_LIST);
      TimedealProductListResponseDto cachedDto =
          objectMapper.convertValue(cachedList, TimedealProductListResponseDto.class);
      return refreshTimedealStatus(cachedDto);
    }

    // 2. ZSET 범위 조회
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    long from = now.minusHours(2).toInstant().toEpochMilli();
    long to = now.plusWeeks(1).toInstant().toEpochMilli();

    Set<Object> dealIds =
        redisTemplate.opsForZSet().rangeByScore(ProductCacheKeys.TIMEDEAL_ZSET, from, to);

    if (dealIds != null && !dealIds.isEmpty()) {
      // 3. 단건 캐시 조회 (ZSET 기반)
      List<TimedealProductSummaryResponseDto> result = new ArrayList<>();
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

      // 4. DB fallback (단건 캐시 MISS만)
      if (!missedPolicyIds.isEmpty()) {
        log.info("[TimedealProductReadService] DB fallback 시작 - size={}", missedPolicyIds.size());

        List<TimedealProductSummaryResponseDto> fallbackList =
            timedealProductQueryService.findAllById(missedPolicyIds);

        fallbackList.parallelStream()
            .forEach(
                dto -> {
                  String key = ProductCacheKeys.timedealSingle(dto.dealId());
                  long ttl =
                      Duration.between(OffsetDateTime.now(ZoneOffset.UTC), dto.dealEndTime())
                              .toSeconds()
                          + 60;
                  redisTemplate.opsForValue().set(key, dto, Duration.ofSeconds(ttl));
                  log.info(
                      "[TimedealProductReadService] DB fallback 캐시 저장 - key={}, TTL={}초", key, ttl);
                });

        result.addAll(fallbackList);
      }

      // 5. 목록 캐시 저장 후 반환
      TimedealProductListResponseDto responseDto = new TimedealProductListResponseDto(result);
      redisTemplate
          .opsForValue()
          .set(ProductCacheKeys.TIMEDEAL_LIST, responseDto, Duration.ofSeconds(60));
      log.info("[TimedealProductReadService] 목록 캐시 저장 - TTL=60초");

      return refreshTimedealStatus(responseDto);
    }

    // 6. ZSET 비어 있음 → fallback: 열려 있는 타임딜 아예 없는지 확인
    log.warn("[TimedealProductReadService] ZSET 캐시 없음 → DB fallback for active 타임딜");

    List<TimedealProductSummaryResponseDto> activeDeals =
        timedealProductQueryService.findUpcomingOrOngoingWithinWeek();

    if (activeDeals.isEmpty()) {
      log.info("[TimedealProductReadService] 현재 열려 있는 타임딜 없음 - 빈 목록 반환");
      return new TimedealProductListResponseDto(List.of());
    }

    // 7. 단건 캐시 + ZSet + 목록 캐시 복구
    activeDeals.parallelStream()
        .forEach(
            dto -> {
              String singleKey = ProductCacheKeys.timedealSingle(dto.dealId());
              long ttl =
                  Duration.between(OffsetDateTime.now(ZoneOffset.UTC), dto.dealEndTime())
                          .toSeconds()
                      + 60;
              redisTemplate.opsForValue().set(singleKey, dto, Duration.ofSeconds(ttl));
              long score = dto.dealStartTime().toInstant().toEpochMilli();
              redisTemplate.opsForZSet().add(ProductCacheKeys.TIMEDEAL_ZSET, dto.dealId(), score);
              log.info("[TimedealProductReadService] 캐시 재등록 - key={}, score={}", singleKey, score);
            });

    TimedealProductListResponseDto rebuiltDto = new TimedealProductListResponseDto(activeDeals);
    redisTemplate
        .opsForValue()
        .set(ProductCacheKeys.TIMEDEAL_LIST, rebuiltDto, Duration.ofSeconds(60));
    log.info("[TimedealProductReadService] 목록 캐시 재등록 - TTL=60초");

    return refreshTimedealStatus(rebuiltDto);
  }

  private TimedealProductListResponseDto refreshTimedealStatus(TimedealProductListResponseDto dto) {
    LocalDateTime nowTime = LocalDateTime.now();
    List<TimedealProductSummaryResponseDto> updated =
        dto.timeDeals().stream()
            .map(
                d ->
                    new TimedealProductSummaryResponseDto(
                        d.dealId(),
                        d.productId(),
                        d.title(),
                        d.description(),
                        d.defaultPrice(),
                        d.discountedPrice(),
                        d.discountedPercentage(),
                        d.stock(),
                        d.imageUrl(),
                        d.dealStartTime(),
                        d.dealEndTime(),
                        d.productStatus(),
                        determineTimeDealStatus(d.dealStartTime(), d.dealEndTime())))
            .toList();
    return new TimedealProductListResponseDto(updated);
  }

  private String determineTimeDealStatus(OffsetDateTime start, OffsetDateTime end) {
    OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
    return (!nowUtc.isBefore(start) && nowUtc.isBefore(end)) ? "ONGOING" : "UPCOMING";
  }
}

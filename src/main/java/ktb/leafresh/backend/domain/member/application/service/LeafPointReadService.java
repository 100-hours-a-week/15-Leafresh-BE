package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.global.exception.LeafPointErrorCode;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberLeafPointQueryRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.TotalLeafPointResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.lock.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeafPointReadService {

  private final StringRedisTemplate redisTemplate;
  private final MemberLeafPointQueryRepository memberLeafPointQueryRepository;

  private static final String TOTAL_LEAF_SUM_KEY = "leafresh:totalLeafPoints:sum";

  @DistributedLock(key = "'leafresh:totalLeafPoints:sum'")
  public TotalLeafPointResponseDto getTotalLeafPoints() {
    try {
      // 락 획득 후 재확인
      String cached = redisTemplate.opsForValue().get(TOTAL_LEAF_SUM_KEY);
      if (cached != null) {
        log.debug("[LeafPointReadService] Redis cache hit after lock: {}", cached);
        return new TotalLeafPointResponseDto(Integer.parseInt(cached));
      }

      int sum = memberLeafPointQueryRepository.getTotalLeafPointSum();
      redisTemplate
          .opsForValue()
          .set(TOTAL_LEAF_SUM_KEY, String.valueOf(sum), Duration.ofHours(24));
      log.info("[LeafPointReadService] Redis cache set after DB fallback: {}", sum);
      return new TotalLeafPointResponseDto(sum);
    } catch (NumberFormatException e) {
      throw new CustomException(LeafPointErrorCode.REDIS_FAILURE);
    } catch (Exception e) {
      throw new CustomException(LeafPointErrorCode.DB_QUERY_FAILED);
    }
  }
}

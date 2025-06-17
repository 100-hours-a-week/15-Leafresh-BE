package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.global.exception.LeafPointErrorCode;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberLeafPointQueryRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.TotalLeafPointResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
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
    private final RedissonClient redissonClient;

    private static final String TOTAL_LEAF_SUM_KEY = "leafresh:totalLeafPoints:sum";
    private static final String TOTAL_LEAF_SUM_LOCK_KEY = "lock:leafresh:totalLeafPoints:sum";

    public TotalLeafPointResponseDto getTotalLeafPoints() {
        try {
            String cached = redisTemplate.opsForValue().get(TOTAL_LEAF_SUM_KEY);
            if (cached != null) {
                log.debug("[LeafPointReadService] Redis cache hit: {}", cached);
                return new TotalLeafPointResponseDto(Integer.parseInt(cached));
            }

            log.warn("[LeafPointReadService] Redis cache miss. Trying Redisson lock...");
            RLock lock = redissonClient.getLock(TOTAL_LEAF_SUM_LOCK_KEY);
            boolean isLocked = lock.tryLock(2, 5, TimeUnit.SECONDS);

            if (isLocked) {
                try {
                    String retryCached = redisTemplate.opsForValue().get(TOTAL_LEAF_SUM_KEY);
                    if (retryCached != null) {
                        log.debug("[LeafPointReadService] Redis cache filled by other thread: {}", retryCached);
                        return new TotalLeafPointResponseDto(Integer.parseInt(retryCached));
                    }

                    int sum = memberLeafPointQueryRepository.getTotalLeafPointSum();
                    redisTemplate.opsForValue().set(
                            TOTAL_LEAF_SUM_KEY, String.valueOf(sum), Duration.ofHours(24)
                    );
                    log.info("[LeafPointReadService] Redis cache set after DB fallback: {}", sum);
                    return new TotalLeafPointResponseDto(sum);
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("[LeafPointReadService] Redisson lock 획득 실패. Waiting and retrying Redis...");
                Thread.sleep(200);
                String retry = redisTemplate.opsForValue().get(TOTAL_LEAF_SUM_KEY);
                if (retry != null) {
                    return new TotalLeafPointResponseDto(Integer.parseInt(retry));
                } else {
                    throw new CustomException(LeafPointErrorCode.REDIS_FAILURE);
                }
            }

        } catch (NumberFormatException e) {
            log.error("[LeafPointReadService] Redis 캐시 값 변환 실패", e);
            throw new CustomException(LeafPointErrorCode.REDIS_FAILURE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(LeafPointErrorCode.REDIS_FAILURE);
        } catch (Exception e) {
            log.error("[LeafPointReadService] 누적 나뭇잎 수 조회 실패", e);
            throw new CustomException(LeafPointErrorCode.DB_QUERY_FAILED);
        }
    }
}

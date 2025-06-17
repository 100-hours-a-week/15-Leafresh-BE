package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.response.VerificationCountResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
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
public class VerificationCountReadService {

    private final StringRedisTemplate redisTemplate;
    private final VerificationCountQueryService verificationCountQueryService;
    private final RedissonClient redissonClient;

    private static final String TOTAL_VERIFICATION_COUNT_KEY = "leafresh:totalVerifications:count";
    private static final String LOCK_KEY = "lock:leafresh:totalVerifications:count";

    public VerificationCountResponseDto getTotalVerificationCount() {
        try {
            String cached = redisTemplate.opsForValue().get(TOTAL_VERIFICATION_COUNT_KEY);
            if (cached != null) {
                log.debug("[VerificationCountReadService] Redis cache hit: {}", cached);
                return new VerificationCountResponseDto(Integer.parseInt(cached));
            }

            log.warn("[VerificationCountReadService] Redis cache miss. Trying Redisson lock...");
            RLock lock = redissonClient.getLock(LOCK_KEY);
            boolean isLocked = lock.tryLock(2, 5, TimeUnit.SECONDS);

            if (isLocked) {
                try {
                    String retry = redisTemplate.opsForValue().get(TOTAL_VERIFICATION_COUNT_KEY);
                    if (retry != null) {
                        log.debug("[VerificationCountReadService] Redis re-check after lock: {}", retry);
                        return new VerificationCountResponseDto(Integer.parseInt(retry));
                    }

                    int total = verificationCountQueryService.getTotalVerificationCountFromDB();
                    redisTemplate.opsForValue()
                            .set(TOTAL_VERIFICATION_COUNT_KEY, String.valueOf(total), Duration.ofHours(24));
                    log.info("[VerificationCountReadService] Redis cache refresh 완료: {}", total);

                    return new VerificationCountResponseDto(total);
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("[VerificationCountReadService] Redisson lock 획득 실패. 대기 후 캐시 재조회");
                Thread.sleep(200);
                String fallback = redisTemplate.opsForValue().get(TOTAL_VERIFICATION_COUNT_KEY);
                if (fallback != null) {
                    return new VerificationCountResponseDto(Integer.parseInt(fallback));
                }
                throw new CustomException(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
            }

        } catch (NumberFormatException e) {
            log.error("[VerificationCountReadService] Redis 캐시 값 파싱 실패", e);
            throw new CustomException(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
        } catch (Exception e) {
            log.error("[VerificationCountReadService] Redis 조회 중 알 수 없는 에러", e);
            throw new CustomException(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
        }
    }
}

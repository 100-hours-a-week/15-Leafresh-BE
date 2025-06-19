package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.response.VerificationCountResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
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
public class VerificationCountReadService {

    private final StringRedisTemplate redisTemplate;
    private final VerificationCountQueryService verificationCountQueryService;

    private static final String TOTAL_VERIFICATION_COUNT_KEY = "leafresh:totalVerifications:count";

    public VerificationCountResponseDto getTotalVerificationCount() {
        try {
            String cached = redisTemplate.opsForValue().get(TOTAL_VERIFICATION_COUNT_KEY);
            if (cached != null) {
                log.debug("[VerificationCountReadService] Redis cache hit: {}", cached);
                return new VerificationCountResponseDto(Integer.parseInt(cached));
            }

            log.warn("[VerificationCountReadService] Redis cache miss. 진입 → lock 메서드 실행");
            return getTotalVerificationCountWithLock();
        } catch (NumberFormatException e) {
            log.error("[VerificationCountReadService] Redis 캐시 파싱 실패", e);
            throw new CustomException(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
        } catch (Exception e) {
            log.error("[VerificationCountReadService] 알 수 없는 예외", e);
            throw new CustomException(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
        }
    }

    @DistributedLock(key = "'totalVerifications'")
    public VerificationCountResponseDto getTotalVerificationCountWithLock() {
        String cached = redisTemplate.opsForValue().get(TOTAL_VERIFICATION_COUNT_KEY);
        if (cached != null) {
            log.debug("[VerificationCountReadService] Lock 이후 캐시 재조회 성공: {}", cached);
            return new VerificationCountResponseDto(Integer.parseInt(cached));
        }

        int total = verificationCountQueryService.getTotalVerificationCountFromDB();
        redisTemplate.opsForValue().set(TOTAL_VERIFICATION_COUNT_KEY, String.valueOf(total), Duration.ofHours(24));
        log.info("[VerificationCountReadService] 캐시 재적재 완료: {}", total);

        return new VerificationCountResponseDto(total);
    }
}

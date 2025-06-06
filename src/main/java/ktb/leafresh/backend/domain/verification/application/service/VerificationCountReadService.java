package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.response.VerificationCountResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCountReadService {

    private final StringRedisTemplate redisTemplate;

    private static final String TOTAL_VERIFICATION_COUNT_KEY = "leafresh:totalVerifications:count";

    public VerificationCountResponseDto getTotalVerificationCount() {
        try {
            String cached = redisTemplate.opsForValue().get(TOTAL_VERIFICATION_COUNT_KEY);
            if (cached != null) {
                log.debug("[VerificationCountReadService] Redis cache hit: {}", cached);
                return new VerificationCountResponseDto(Integer.parseInt(cached));
            }

            log.warn("[VerificationCountReadService] Redis cache miss. Returning 0.");
            return new VerificationCountResponseDto(0);

        } catch (NumberFormatException e) {
            log.error("[VerificationCountReadService] Redis 캐시 값 파싱 실패", e);
            throw new CustomException(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
        } catch (Exception e) {
            log.error("[VerificationCountReadService] Redis 조회 중 알 수 없는 에러", e);
            throw new CustomException(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
        }
    }
}

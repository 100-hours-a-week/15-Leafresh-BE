package ktb.leafresh.backend.global.initializer;

import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.verification.application.service.VerificationCountQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCountInitializer {

    private final VerificationCountQueryService queryService;
    private final StringRedisTemplate redisTemplate;

    private static final String TOTAL_VERIFICATION_COUNT_KEY = "leafresh:totalVerifications:count";

    @PostConstruct
    public void initializeVerificationCountCache() {
        try {
            int total = queryService.getTotalVerificationCountFromDB();
            redisTemplate.opsForValue().set(TOTAL_VERIFICATION_COUNT_KEY, String.valueOf(total));
            log.info("[VerificationCountInitializer] Redis 누적 인증 수 초기화 완료: {}", total);
        } catch (Exception e) {
            log.error("[VerificationCountInitializer] Redis 초기화 실패", e);
        }
    }
}

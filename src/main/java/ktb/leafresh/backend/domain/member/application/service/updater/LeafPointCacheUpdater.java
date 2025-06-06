package ktb.leafresh.backend.domain.member.application.service.updater;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeafPointCacheUpdater {

    private final StringRedisTemplate redisTemplate;

    private static final String TOTAL_LEAF_SUM_KEY = "leafresh:totalLeafPoints:sum";

    /**
     * Redis 기반 누적 나뭇잎 포인트 캐시 증가 처리
     *
     * - RDB 트랜잭션과는 분리되어 있으며, 커밋 여부와 관계없이 즉시 Redis에 반영됨
     * - 캐시 반영 실패 시 기능에는 영향 없음 (Redis는 통계성 보조 지표로만 활용됨)
     * - 실패 시 로그 기록 후 후속 복구 처리 필요
     */
    public void rewardLeafPoints(Member member, int amount) {
        try {
            redisTemplate.opsForValue().increment(TOTAL_LEAF_SUM_KEY, amount);
            log.debug("[LeafPointCacheUpdater] Redis 누적 나뭇잎 증가 +{} 반영 완료", amount);
        } catch (Exception e) {
            log.warn("[LeafPointCacheUpdater] Redis 증가 실패 - 추후 초기화 필요: {}", e.getMessage());
        }
    }
}

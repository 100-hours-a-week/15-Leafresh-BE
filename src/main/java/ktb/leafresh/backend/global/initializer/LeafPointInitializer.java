package ktb.leafresh.backend.global.initializer;

import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberLeafPointQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeafPointInitializer {

    private final MemberLeafPointQueryRepository memberLeafPointQueryRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String TOTAL_LEAF_SUM_KEY = "leafresh:totalLeafPoints:sum";

    @PostConstruct
    public void initializeLeafPointCache() {
        try {
            int sum = memberLeafPointQueryRepository.getTotalLeafPointSum();
            redisTemplate.opsForValue().set(TOTAL_LEAF_SUM_KEY, String.valueOf(sum));
            log.info("[LeafPointInitializer] Redis 누적 나뭇잎 합계 초기화 완료: {}", sum);
        } catch (Exception e) {
            log.error("[LeafPointInitializer] Redis 초기화 실패", e);
        }
    }
}

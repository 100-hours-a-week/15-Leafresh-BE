package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberLeafPointRewardService {

    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String TOTAL_LEAF_SUM_KEY = "leafresh:totalLeafPoints:sum";

    /**
     * 트랜잭션 내에서만 Redis 증가 → DB 커밋 성공 시 Redis도 반영
     */
    @Transactional
    public void rewardLeafPoints(Member member, int amount) {
        member.addLeafPoints(amount);
        memberRepository.save(member); // flush 포함됨

        try {
            redisTemplate.opsForValue().increment(TOTAL_LEAF_SUM_KEY, amount);
            log.debug("[MemberService] Redis 누적 나뭇잎 증가 +{} 반영 완료", amount);
        } catch (Exception e) {
            log.warn("[MemberService] Redis 증가 실패 - 추후 초기화 필요: {}", e.getMessage());
        }
    }
}

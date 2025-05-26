package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.global.exception.LeafPointErrorCode;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberLeafPointQueryRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.TotalLeafPointResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeafPointReadService {

    private final StringRedisTemplate redisTemplate;
    private final MemberLeafPointQueryRepository memberLeafPointQueryRepository;

    private static final String TOTAL_LEAF_SUM_KEY = "leafresh:totalLeafPoints:sum";

    public TotalLeafPointResponseDto getTotalLeafPoints() {
        try {
            String cached = redisTemplate.opsForValue().get(TOTAL_LEAF_SUM_KEY);
            if (cached != null) {
                log.debug("[LeafPointReadService] Redis cache hit: {}", cached);
                return new TotalLeafPointResponseDto(Integer.parseInt(cached));
            }
            log.debug("[LeafPointReadService] Redis cache miss. Querying DB...");

            int sum = memberLeafPointQueryRepository.getTotalLeafPointSum();
            redisTemplate.opsForValue().set(TOTAL_LEAF_SUM_KEY, String.valueOf(sum));
            return new TotalLeafPointResponseDto(sum);
        } catch (NumberFormatException e) {
            log.error("[LeafPointReadService] Redis 캐싱 값 변환 실패", e);
            throw new CustomException(LeafPointErrorCode.REDIS_FAILURE);
        } catch (Exception e) {
            log.error("[LeafPointReadService] 누적 나뭇잎 DB 조회 실패", e);
            throw new CustomException(LeafPointErrorCode.DB_QUERY_FAILED);
        }
    }
}

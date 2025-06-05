package ktb.leafresh.backend.global.util.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@RequiredArgsConstructor
@Slf4j
@Service
public class VerificationStatRedisLuaService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String INCREASE_VIEW_COUNT_LUA = """
        local hashKey = KEYS[1]
        local dirtySetKey = KEYS[2]
        local verificationId = ARGV[1]
        local ttlSeconds = tonumber(ARGV[2])

        -- 뷰 카운트 증가
        redis.call("HINCRBY", hashKey, "viewCount", 1)

        -- dirty set에 등록
        redis.call("SADD", dirtySetKey, verificationId)

        -- TTL 설정 (매번 갱신)
        redis.call("EXPIRE", hashKey, ttlSeconds)

        return 1
    """;

    public void increaseVerificationViewCount(Long verificationId) {
        String statKey = "verification:stat:" + verificationId;
        String dirtySetKey = "verification:stat:dirty";

        try {
            stringRedisTemplate.execute(
                    new DefaultRedisScript<>(INCREASE_VIEW_COUNT_LUA, Long.class),
                    Arrays.asList(statKey, dirtySetKey),
                    verificationId.toString(), String.valueOf(60 * 60 * 24) // 24시간 TTL
            );
            log.debug("[Lua] 조회수 증가 완료 - verificationId={}", verificationId);
        } catch (Exception e) {
            log.warn("[Lua] 조회수 증가 실패 - verificationId={}, err={}", verificationId, e.getMessage(), e);
        }
    }
}

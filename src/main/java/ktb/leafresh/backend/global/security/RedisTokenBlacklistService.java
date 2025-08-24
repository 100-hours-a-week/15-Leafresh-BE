package ktb.leafresh.backend.global.security;

import io.rebloom.client.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!swagger")
public class RedisTokenBlacklistService implements TokenBlacklistService {

  private final StringRedisTemplate redisTemplate;

  private final Client bloomClient;

  @Value("${jwt.bloom-filter.key:accessTokenBlacklist}")
  private String BLOOM_FILTER_NAME;

  @Override
  public void blacklistAccessToken(String accessToken, long expirationTimeMillis) {
    // Bloom Filter에 등록
    bloomClient.add(BLOOM_FILTER_NAME, accessToken);

    // TTL 보장용 키 저장
    String key = "blacklist:" + accessToken;
    redisTemplate.opsForValue().set(key, "true", expirationTimeMillis, TimeUnit.MILLISECONDS);

    log.info("[블랙리스트 등록 완료] token={}, TTL={}ms", accessToken, expirationTimeMillis);
  }

  @Override
  public boolean isBlacklisted(String accessToken) {
    boolean mightExist = bloomClient.exists(BLOOM_FILTER_NAME, accessToken);
    if (!mightExist) return false;

    return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + accessToken));
  }
}

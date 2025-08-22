package ktb.leafresh.backend.global.util.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Slf4j
@Service
public class StockRedisLuaService {

  private final StringRedisTemplate stringRedisTemplate;

  private static final String DECREASE_STOCK_LUA =
      """
        local stock = tonumber(redis.call("GET", KEYS[1]))
        local qty = tonumber(ARGV[1])
        if stock == nil then return -1 end
        if stock < qty then return -2 end
        return redis.call("DECRBY", KEYS[1], qty)
    """;

  public Long decreaseStock(String key, int quantity) {
    log.debug("[RedisLuaService] key={}, quantity={}", key, quantity);
    try {
      return stringRedisTemplate.execute(
          new DefaultRedisScript<>(DECREASE_STOCK_LUA, Long.class),
          Collections.singletonList(key),
          String.valueOf(quantity));
    } catch (Exception e) {
      log.error("[RedisLua 오류] key={}, quantity={}, message={}", key, quantity, e.getMessage(), e);
      throw e;
    }
  }
}

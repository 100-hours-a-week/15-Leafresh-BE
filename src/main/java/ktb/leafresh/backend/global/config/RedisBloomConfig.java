package ktb.leafresh.backend.global.config;

import io.rebloom.client.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisBloomConfig {

  @Value("${redis.bloom.host}")
  private String redisBloomHost;

  @Value("${redis.bloom.port}")
  private int redisBloomPort;

  @Bean
  public Client bloomClient() {
    return new Client(redisBloomHost, redisBloomPort);
  }
}

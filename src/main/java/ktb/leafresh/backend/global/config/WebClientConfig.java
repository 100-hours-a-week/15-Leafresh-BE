package ktb.leafresh.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // 기본 WebClient
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    // AI 서버 전용 WebClient
    @Bean(name = "aiServerWebClient")
    public WebClient aiServerWebClient(@Value("${ai-server.base-url}") String aiServerBaseUrl) {
        return WebClient.builder()
                .baseUrl(aiServerBaseUrl)
                .build();
    }
}

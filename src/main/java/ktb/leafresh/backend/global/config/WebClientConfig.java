package ktb.leafresh.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean(name = "textAiWebClient")
    public WebClient textAiWebClient(@Value("${ai-server.text-base-url}") String textAiBaseUrl) {
        return WebClient.builder()
                .baseUrl(textAiBaseUrl)
                .build();
    }

    @Bean(name = "imageAiWebClient")
    public WebClient imageAiWebClient(@Value("${ai-server.image-base-url}") String imageAiBaseUrl) {
        return WebClient.builder()
                .baseUrl(imageAiBaseUrl)
                .build();
    }

    @Bean(name = "makeChallengeAiWebClient")
    public WebClient makeChallengeAiWebClient(@Value("${ai-server.make-challenge-base-url}") String makeChallengeBaseUrl) {
        return WebClient.builder()
                .baseUrl(makeChallengeBaseUrl)
                .build();
    }
}

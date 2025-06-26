package ktb.leafresh.backend.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class CorsConfig {

    private final SecurityProperties securityProperties;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        securityProperties.getAllowedOrigins().forEach(config::addAllowedOriginPattern); // ★ 여기!
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

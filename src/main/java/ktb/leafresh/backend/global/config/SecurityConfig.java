package ktb.leafresh.backend.global.config;

import ktb.leafresh.backend.global.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthCookieProvider authCookieProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Spring Security 기본 CORS 설정 비활성화
                .cors(withDefaults())

                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // CORS 필터 등록 (JWT 인증 필터보다 먼저 실행)
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

                // JWT 예외 처리 설정
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // 세션을 사용하지 않음 (STATELESS)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 인증, 인가 설정
                .authorizeHttpRequests(auth -> auth

                        // images
                        .requestMatchers("/s3/images/presigned-url").permitAll()

                        // 메인 페이지
                        .requestMatchers("/api/leaves/count").permitAll()
                        .requestMatchers("/api/challenges/verifications/count").permitAll()

                        // 테스트 컨트롤러용 허용 경로 추가
                        .requestMatchers("/spring/**").permitAll()

                        // 소셜 로그인 요청(리다이렉트), 콜백, 로그아웃 모두 허용
                        .requestMatchers("/oauth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/nickname").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members").permitAll()

                        // 단체 챌린지
                        .requestMatchers(HttpMethod.GET, "/api/challenges/group/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/challenges/group").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/challenges/group/{challengeId:\\d+}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/challenges/events").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/challenges/group/{challengeId:\\d+}/verifications").permitAll()

                        // 그 외 단체 챌린지 API는 인증 필요
                        .requestMatchers("/api/challenges/group/**").authenticated()

                        // 개인 챌린지
                        .requestMatchers(HttpMethod.GET, "/api/challenges/personal").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/challenges/personal/{challengeId:\\d+}").permitAll()

                        // 그 외 개인 챌린지 API는 인증 필요
                        .requestMatchers("/api/chalglenges/personal/**").authenticated()

                        // AI로부터 챌린지 인증 결과 받는 API
                        .requestMatchers(HttpMethod.POST, "/api/verifications/**").permitAll()

                        // 챗봇
                        .requestMatchers(HttpMethod.POST, "/api/chatbot/recommendation/base-info").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/chatbot/recommendation/free-text").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/chatbot/recommendation/base-info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/chatbot/recommendation/free-text").permitAll()

                        // 피드백
                        .requestMatchers(HttpMethod.POST, "/api/members/feedback/result").permitAll()

                        // 상점
                        .requestMatchers(HttpMethod.GET, "/api/store/products/timedeals").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/store/products").permitAll()

                        // Swagger/OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/actuator/**"
                        ).permitAll()

                        // CORS preflight 요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 그 외 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 기본 로그인 및 HTTP Basic 인증 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())

                // JWT 필터 적용
                .apply(new JwtSecurityConfig(tokenProvider, tokenBlacklistService, authCookieProvider));

        return http.build();
    }
}

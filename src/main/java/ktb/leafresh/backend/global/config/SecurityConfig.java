package ktb.leafresh.backend.global.config;

import ktb.leafresh.backend.global.security.JwtAccessDeniedHandler;
import ktb.leafresh.backend.global.security.JwtAuthenticationEntryPoint;
import ktb.leafresh.backend.global.security.TokenBlacklistService;
import ktb.leafresh.backend.global.security.TokenProvider;
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
                        // 테스트 컨트롤러용 허용 경로 추가
                        .requestMatchers("/spring/**").permitAll()

                        // 소셜 로그인 요청(리다이렉트), 콜백, 로그아웃 모두 허용
                        .requestMatchers("/oauth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/nickname").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/signup").permitAll()

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
                .apply(new JwtSecurityConfig(tokenProvider, tokenBlacklistService));

        return http.build();
    }
}

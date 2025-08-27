package ktb.leafresh.backend.global.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("JwtProvider 테스트")
class JwtProviderTest {

    private JwtProvider jwtProvider;
    private final String PLAIN_SECRET_KEY = "test-secret-key-that-is-at-least-32-characters-long-for-jwt-signing";
    private final String BASE64_SECRET_KEY = Encoders.BASE64.encode(PLAIN_SECRET_KEY.getBytes());
    private Key key;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = Decoders.BASE64.decode(BASE64_SECRET_KEY);
        key = Keys.hmacShaKeyFor(keyBytes);
        
        jwtProvider = new JwtProvider(BASE64_SECRET_KEY);
    }

    @Test
    @DisplayName("JWT Provider 생성 - 성공")
    void createJwtProvider_Success() {
        // when
        JwtProvider provider = new JwtProvider(BASE64_SECRET_KEY);

        // then
        assertThat(provider).isNotNull();
        assertThat(provider.getKey()).isNotNull();
    }

    @Test
    @DisplayName("토큰 생성 - 성공")
    void generateTokenDto_Success() {
        // given
        Long memberId = 1L;
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // when
        TokenDto tokenDto = jwtProvider.generateTokenDto(memberId, authorities);

        // then
        assertAll(
                () -> assertThat(tokenDto.getAccessToken()).isNotNull(),
                () -> assertThat(tokenDto.getRefreshToken()).isNotNull(),
                () -> assertThat(tokenDto.getAccessTokenExpiresIn()).isGreaterThan(System.currentTimeMillis())
        );
    }

    @Test
    @DisplayName("빈 권한 목록으로 토큰 생성 - 성공")
    void generateTokenDto_WithEmptyAuthorities_Success() {
        // given
        Long memberId = 1L;
        List<GrantedAuthority> authorities = List.of();

        // when
        TokenDto tokenDto = jwtProvider.generateTokenDto(memberId, authorities);

        // then
        assertAll(
                () -> assertThat(tokenDto.getAccessToken()).isNotNull(),
                () -> assertThat(tokenDto.getRefreshToken()).isNotNull(),
                () -> assertThat(tokenDto.getAccessTokenExpiresIn()).isGreaterThan(System.currentTimeMillis())
        );
    }

    @Test
    @DisplayName("여러 권한으로 토큰 생성 - 성공")
    void generateTokenDto_WithMultipleAuthorities_Success() {
        // given
        Long memberId = 1L;
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        // when
        TokenDto tokenDto = jwtProvider.generateTokenDto(memberId, authorities);

        // then
        assertAll(
                () -> assertThat(tokenDto.getAccessToken()).isNotNull(),
                () -> assertThat(tokenDto.getRefreshToken()).isNotNull(),
                () -> assertThat(tokenDto.getAccessTokenExpiresIn()).isGreaterThan(System.currentTimeMillis())
        );
        
        // and when - 토큰에서 권한 정보 확인
        String accessToken = tokenDto.getAccessToken();
        String authorities_claim = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .get("auth", String.class);
        
        // then
        assertThat(authorities_claim).contains("ROLE_USER,ROLE_ADMIN");
    }

    @Test
    @DisplayName("토큰에서 회원 ID 확인 - 성공")
    void generateTokenDto_CheckMemberId_Success() {
        // given
        Long memberId = 12345L;
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // when
        TokenDto tokenDto = jwtProvider.generateTokenDto(memberId, authorities);
        
        // and when - 토큰에서 회원 ID 확인
        String subject = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(tokenDto.getAccessToken())
                .getBody()
                .getSubject();

        // then
        assertThat(subject).isEqualTo(memberId.toString());
    }

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰의 만료시간 차이 확인 - 성공")
    void generateTokenDto_CheckExpirationDifference_Success() {
        // given
        Long memberId = 1L;
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // when
        TokenDto tokenDto = jwtProvider.generateTokenDto(memberId, authorities);
        
        // and when - 각 토큰의 만료시간 추출
        Date accessTokenExpiry = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(tokenDto.getAccessToken())
                .getBody()
                .getExpiration();
                
        Date refreshTokenExpiry = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(tokenDto.getRefreshToken())
                .getBody()
                .getExpiration();

        // then - 리프레시 토큰이 액세스 토큰보다 훨씬 늦게 만료되어야 함
        assertThat(refreshTokenExpiry.getTime()).isGreaterThan(accessTokenExpiry.getTime());
        
        // 대략적인 시간 차이 확인 (6일 이상 차이)
        long timeDifference = refreshTokenExpiry.getTime() - accessTokenExpiry.getTime();
        long sixDaysInMs = 6 * 24 * 60 * 60 * 1000L;
        assertThat(timeDifference).isGreaterThan(sixDaysInMs);
    }

    @Test
    @DisplayName("유효한 토큰의 만료 시간 조회 - 성공")
    void getExpiration_ValidToken_Success() {
        // given
        Long memberId = 1L;
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        TokenDto tokenDto = jwtProvider.generateTokenDto(memberId, authorities);

        // when
        long expiration = jwtProvider.getExpiration(tokenDto.getAccessToken());

        // then
        // JWT는 초 단위로 만료시간을 저장하므로, 토큰에서 직접 추출한 만료시간과 비교
        Date tokenExpiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(tokenDto.getAccessToken())
                .getBody()
                .getExpiration();
        
        assertThat(expiration).isEqualTo(tokenExpiration.getTime());
    }

    @Test
    @DisplayName("만료된 토큰의 만료 시간 조회 - 성공")
    void getExpiration_ExpiredToken_Success() {
        // given
        String expiredToken = createExpiredToken(1L);

        // when
        long expiration = jwtProvider.getExpiration(expiredToken);

        // then
        assertThat(expiration).isLessThan(System.currentTimeMillis());
    }

    @Test
    @DisplayName("State 토큰 생성 - 성공")
    void generateStateToken_Success() {
        // given
        String origin = "https://example.com";

        // when
        String stateToken = jwtProvider.generateStateToken(origin);

        // then
        assertThat(stateToken).isNotNull();
        assertThat(stateToken.split("\\.")).hasSize(3); // JWT 형태 확인 (header.payload.signature)
    }

    @Test
    @DisplayName("State 토큰 파싱 - 성공")
    void parseStateToken_Success() {
        // given
        String origin = "https://example.com";
        String stateToken = jwtProvider.generateStateToken(origin);

        // when
        String parsedOrigin = jwtProvider.parseStateToken(stateToken);

        // then
        assertThat(parsedOrigin).isEqualTo(origin);
    }

    @Test
    @DisplayName("State 토큰에 subject 확인 - 성공")
    void generateStateToken_CheckSubject_Success() {
        // given
        String origin = "https://test.com";

        // when
        String stateToken = jwtProvider.generateStateToken(origin);
        
        // and when - 토큰에서 subject 확인
        String subject = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(stateToken)
                .getBody()
                .getSubject();

        // then
        assertThat(subject).isEqualTo("oauth_state");
    }

    @Test
    @DisplayName("State 토큰 만료시간 확인 - 성공")
    void generateStateToken_CheckExpiration_Success() {
        // given
        String origin = "https://test.com";
        long beforeGeneration = System.currentTimeMillis();

        // when
        String stateToken = jwtProvider.generateStateToken(origin);
        
        // and when - 토큰에서 만료시간 확인
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(stateToken)
                .getBody()
                .getExpiration();
        
        long afterGeneration = System.currentTimeMillis();

        // then - 3분(180초) 후에 만료되어야 함
        long expirationTime = expiration.getTime();
        long expectedMinExpiration = beforeGeneration + (3 * 60 * 1000) - 1000; // 1초 여유
        long expectedMaxExpiration = afterGeneration + (3 * 60 * 1000) + 1000;  // 1초 여유
        
        assertThat(expirationTime)
                .isGreaterThan(expectedMinExpiration)
                .isLessThan(expectedMaxExpiration);
    }

    @Test
    @DisplayName("만료된 State 토큰 파싱 - 실패")
    void parseStateToken_ExpiredToken_ThrowsException() {
        // given
        String expiredStateToken = createExpiredStateToken("https://example.com");

        // when & then
        assertThatThrownBy(() -> jwtProvider.parseStateToken(expiredStateToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("잘못된 State 토큰 파싱 - 실패")
    void parseStateToken_InvalidToken_ThrowsException() {
        // given
        String invalidToken = "invalid.token.format";

        // when & then
        assertThatThrownBy(() -> jwtProvider.parseStateToken(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("null origin으로 State 토큰 생성 - 성공")
    void generateStateToken_WithNullOrigin_Success() {
        // when
        String stateToken = jwtProvider.generateStateToken(null);

        // then
        assertThat(stateToken).isNotNull();
        
        // and when
        String parsedOrigin = jwtProvider.parseStateToken(stateToken);
        
        // then
        assertThat(parsedOrigin).isNull();
    }

    @Test
    @DisplayName("빈 문자열 origin으로 State 토큰 생성 - 성공")
    void generateStateToken_WithEmptyOrigin_Success() {
        // given
        String origin = "";

        // when
        String stateToken = jwtProvider.generateStateToken(origin);

        // then
        assertThat(stateToken).isNotNull();
        
        // and when
        String parsedOrigin = jwtProvider.parseStateToken(stateToken);
        
        // then
        assertThat(parsedOrigin).isEqualTo("");
    }

    @Test
    @DisplayName("특수문자가 포함된 origin으로 State 토큰 생성 - 성공")
    void generateStateToken_WithSpecialCharactersOrigin_Success() {
        // given
        String origin = "https://test.com/callback?param1=value1&param2=값2";

        // when
        String stateToken = jwtProvider.generateStateToken(origin);

        // then
        assertThat(stateToken).isNotNull();
        
        // and when
        String parsedOrigin = jwtProvider.parseStateToken(stateToken);
        
        // then
        assertThat(parsedOrigin).isEqualTo(origin);
    }

    @Test
    @DisplayName("키 접근 - 성공")
    void getKey_Success() {
        // when
        Key actualKey = jwtProvider.getKey();

        // then
        assertThat(actualKey).isNotNull();
        assertThat(actualKey.getAlgorithm()).isEqualTo("HmacSHA512");
    }

    @Test
    @DisplayName("null memberId로 토큰 생성 - 실패")
    void generateTokenDto_WithNullMemberId_ThrowsException() {
        // given
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // when & then
        assertThatThrownBy(() -> jwtProvider.generateTokenDto(null, authorities))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("null authorities로 토큰 생성 - 실패")
    void generateTokenDto_WithNullAuthorities_ThrowsException() {
        // given
        Long memberId = 1L;

        // when & then
        assertThatThrownBy(() -> jwtProvider.generateTokenDto(memberId, null))
                .isInstanceOf(Exception.class);
    }

    private String createExpiredToken(Long memberId) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now - 1000 * 60); // 1분 전 만료

        return Jwts.builder()
                .setSubject(memberId.toString())
                .claim("auth", "ROLE_USER")
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String createExpiredStateToken(String origin) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now - 1000 * 60); // 1분 전 만료

        return Jwts.builder()
                .setSubject("oauth_state")
                .claim("origin", origin)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}

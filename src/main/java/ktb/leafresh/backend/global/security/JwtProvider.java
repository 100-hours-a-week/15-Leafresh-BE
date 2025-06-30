package ktb.leafresh.backend.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

    @Getter
    private final Key key;

    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public long getExpiration(String token) {
        try {
            return parseClaims(token).getExpiration().getTime();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getExpiration().getTime();
        }
    }

    public TokenDto generateTokenDto(Long memberId, Collection<? extends GrantedAuthority> authorities) {
        String authorityString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = System.currentTimeMillis();

        // AccessToken
        Date accessTokenExpiresAt = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .setSubject(memberId.toString())
                .claim(AUTHORITIES_KEY, authorityString)
                .setExpiration(accessTokenExpiresAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // RefreshToken
        Date refreshTokenExpiresAt = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
        String refreshToken = Jwts.builder()
                .setSubject(memberId.toString())
                .setExpiration(refreshTokenExpiresAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresAt.getTime())
                .refreshToken(refreshToken)
                .build();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    // OAuth 인가 요청 시 state JWT 생성
    public String generateStateToken(String origin) {
        return Jwts.builder()
                .setSubject("oauth_state")
                .claim("origin", origin)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 3)) // 3분 유효
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // 콜백 시 state JWT 복호화
    public String parseStateToken(String stateToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(stateToken)
                .getBody();

        return claims.get("origin", String.class);
    }
}

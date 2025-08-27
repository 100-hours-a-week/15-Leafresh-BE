package ktb.leafresh.backend.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.enums.Role;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenProvider 테스트")
class TokenProviderTest {

    @Mock
    private MemberRepository memberRepository;

    private TokenProvider tokenProvider;
    private Member testMember;
    private String secretKey;

    @BeforeEach
    void setUp() {
        // JWT HS512 알고리즘을 위한 안전한 키 생성
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        secretKey = Encoders.BASE64.encode(key.getEncoded());

        tokenProvider = new TokenProvider(secretKey, memberRepository);

        // 테스트용 멤버 생성
        testMember = Member.builder()
                .id(1L)
                .email("test@test.com")
                .password("password")
                .nickname("testUser")
                .role(Role.USER)
                .build();

        // Member 엔티티의 id 필드를 직접 설정
        ReflectionTestUtils.setField(testMember, "id", 1L);
    }

    @Test
    @DisplayName("토큰 생성 성공")
    void generateTokenDto_Success() {
        // given
        Long memberId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));

        // when
        TokenDto tokenDto = tokenProvider.generateTokenDto(memberId);

        // then
        assertThat(tokenDto).isNotNull();
        assertThat(tokenDto.getAccessToken()).isNotEmpty();
        assertThat(tokenDto.getRefreshToken()).isNotEmpty();
        assertThat(tokenDto.getAccessTokenExpiresIn()).isPositive();

        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 멤버로 토큰 생성 시 예외 발생")
    void generateTokenDto_MemberNotFound() {
        // given
        Long memberId = 999L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tokenProvider.generateTokenDto(memberId))
                .isInstanceOf(CustomException.class);

        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("토큰에서 Subject 추출 성공")
    void getSubject_Success() {
        // given
        Long memberId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));

        TokenDto tokenDto = tokenProvider.generateTokenDto(memberId);
        String accessToken = tokenDto.getAccessToken();

        // when
        String subject = tokenProvider.getSubject(accessToken);

        // then
        assertThat(subject).isEqualTo(memberId.toString());
    }

    @Test
    @DisplayName("유효한 토큰으로 Authentication 생성 성공")
    void getAuthentication_Success() {
        // given
        Long memberId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));

        TokenDto tokenDto = tokenProvider.generateTokenDto(memberId);
        String accessToken = tokenDto.getAccessToken();

        // when
        Authentication authentication = tokenProvider.getAuthentication(accessToken);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(CustomUserDetails.class);
        assertThat(authentication.getAuthorities()).hasSize(1);
        assertThat(authentication.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("USER");
    }

    @Test
    @DisplayName("토큰에서 존재하지 않는 멤버 조회 시 예외 발생")
    void getAuthentication_MemberNotFound() {
        // given
        Long memberId = 1L;
        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(testMember))  // 토큰 생성 시에는 존재
                .willReturn(Optional.empty());        // 인증 시에는 존재하지 않음

        TokenDto tokenDto = tokenProvider.generateTokenDto(memberId);
        String accessToken = tokenDto.getAccessToken();

        // when & then
        assertThatThrownBy(() -> tokenProvider.getAuthentication(accessToken))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateToken_Valid() {
        // given
        Long memberId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));

        TokenDto tokenDto = tokenProvider.generateTokenDto(memberId);
        String accessToken = tokenDto.getAccessToken();

        // when
        boolean isValid = tokenProvider.validateToken(accessToken);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 검증 실패")
    void validateToken_Invalid() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when
        boolean isValid = tokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void validateToken_Empty() {
        // given
        String emptyToken = "";

        // when
        boolean isValid = tokenProvider.validateToken(emptyToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null 토큰 검증 실패")
    void validateToken_Null() {
        // when
        boolean isValid = tokenProvider.validateToken(null);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰으로 Authentication 생성 시 Claims 파싱 성공")
    void getAuthentication_WithExpiredToken() {
        // given
        Long memberId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));

        // 매우 짧은 만료 시간으로 토큰 생성 (즉시 만료)
        long pastTime = System.currentTimeMillis() - 1000; // 1초 전 만료

        String expiredToken = Jwts.builder()
                .setSubject(memberId.toString())
                .claim("auth", "USER")
                .setExpiration(new java.util.Date(pastTime))
                .signWith(tokenProvider.getKey(), SignatureAlgorithm.HS512)
                .compact();

        // when & then
        // 만료된 토큰이라도 parseClaims에서 Claims를 정상적으로 추출하고 Authentication을 생성해야 함
        assertThatCode(() -> {
            Authentication authentication = tokenProvider.getAuthentication(expiredToken);
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isInstanceOf(CustomUserDetails.class);
        }).doesNotThrowAnyException();

        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("토큰 생성 시 권한 정보가 올바르게 포함됨")
    void generateTokenDto_WithCorrectAuthorities() {
        // given
        Long memberId = 1L;
        Member adminMember = Member.builder()
                .id(memberId)
                .email("admin@test.com")
                .password("password")
                .nickname("adminUser")
                .role(Role.ADMIN)
                .build();
        ReflectionTestUtils.setField(adminMember, "id", memberId);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(adminMember));

        // when
        TokenDto tokenDto = tokenProvider.generateTokenDto(memberId);
        String accessToken = tokenDto.getAccessToken();

        // JWT 토큰 직접 파싱하여 권한 확인
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(tokenProvider.getKey())
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        // then
        assertThat(claims.get("auth")).isEqualTo("ADMIN");
        assertThat(claims.getSubject()).isEqualTo(memberId.toString());
    }
}
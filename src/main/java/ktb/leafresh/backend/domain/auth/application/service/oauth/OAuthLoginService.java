package ktb.leafresh.backend.domain.auth.application.service.oauth;

import ktb.leafresh.backend.domain.auth.application.dto.OAuthUserInfoDto;
import ktb.leafresh.backend.domain.auth.application.factory.OAuthTokenFactory;
import ktb.leafresh.backend.domain.auth.application.service.jwt.JwtLogoutService;
import ktb.leafresh.backend.domain.auth.domain.entity.RefreshToken;
import ktb.leafresh.backend.domain.auth.infrastructure.client.OAuthKakaoService;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthTokenResponseDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.RefreshTokenRepository;
import ktb.leafresh.backend.global.security.*;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    @Getter
    @Value("${kakao.client-id}")
    private String clientId;

    @Getter
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final OAuthKakaoService oAuthKakaoService;
    private final JwtLogoutService jwtLogoutService;
    private final JwtProvider jwtProvider;
    private final TokenProvider tokenProvider;
    private final OAuthTokenFactory tokenFactory;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthCookieProvider authCookieProvider;

    public String getRedirectUrl() {
        return "https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code";
    }

    @Transactional
    public OAuthTokenResponseDto loginWithKakao(String authorizationCode) {
        OAuthUserInfoDto userInfo = oAuthKakaoService.getUserInfo(authorizationCode);
        Optional<Member> optionalMember = memberRepository.findByEmail(userInfo.getEmail());

        if (optionalMember.isPresent()) {
            return createTokenResponseForExistingMember(optionalMember.get(), userInfo);
        }

        return createResponseForNewUser(userInfo);
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        jwtLogoutService.logout(accessToken, refreshToken);
    }

    public OAuthTokenResponseDto reissueToken(String refreshToken) {
        String memberId = tokenProvider.getSubject(refreshToken);
        RefreshToken savedToken = validateRefreshToken(refreshToken, memberId);
        Member member = findMemberOrThrow(Long.parseLong(memberId));

        TokenDto tokenDto = jwtProvider.generateTokenDto(member.getId(), createAuthentication(member).getAuthorities());
        return tokenFactory.create(member, tokenDto);
    }

    public ResponseCookie createAccessTokenCookie(String accessToken, Long expiresIn) {
        return authCookieProvider.createAccessTokenCookie(accessToken, expiresIn);
    }

    private OAuthTokenResponseDto createTokenResponseForExistingMember(Member member, OAuthUserInfoDto userInfo) {
        Authentication authentication = createAuthentication(member);
        TokenDto tokenDto = jwtProvider.generateTokenDto(member.getId(), authentication.getAuthorities());

        saveRefreshToken(member.getId(), tokenDto.getRefreshToken());

        return tokenFactory.create(member, tokenDto);
    }

    private Authentication createAuthentication(Member member) {
        UserDetails userDetails = new User(
                member.getId().toString(),
                "",
                List.of(new SimpleGrantedAuthority(member.getRole().name()))
        );
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private void saveRefreshToken(Long memberId, String refreshTokenValue) {
        RefreshToken refreshToken = RefreshToken.builder()
                .rtKey(String.valueOf(memberId))
                .rtValue(refreshTokenValue)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private OAuthTokenResponseDto createResponseForNewUser(OAuthUserInfoDto userInfo) {
        return OAuthTokenResponseDto.builder()
                .nickname("사용자" + System.currentTimeMillis())
                .imageUrl(userInfo.getProfileImageUrl())
                .accessToken(null)
                .refreshToken(null)
                .accessTokenExpiresIn(null)
                .grantType("NONE")
                .providerId(userInfo.getProviderId())
                .email(userInfo.getEmail())
                .build();
    }

    private RefreshToken validateRefreshToken(String refreshToken, String memberId) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken saved = refreshTokenRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!saved.getRtValue().equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return saved;
    }

    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}

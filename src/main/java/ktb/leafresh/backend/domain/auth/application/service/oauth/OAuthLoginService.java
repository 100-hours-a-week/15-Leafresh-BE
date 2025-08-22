package ktb.leafresh.backend.domain.auth.application.service.oauth;

import ktb.leafresh.backend.domain.auth.application.dto.OAuthUserInfoDto;
import ktb.leafresh.backend.domain.auth.application.factory.OAuthTokenFactory;
import ktb.leafresh.backend.domain.auth.application.service.jwt.JwtLogoutService;
import ktb.leafresh.backend.domain.auth.domain.entity.RefreshToken;
import ktb.leafresh.backend.domain.auth.infrastructure.client.OAuthKakaoService;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthTokenResponseDto;
import ktb.leafresh.backend.domain.member.application.service.RewardGrantService;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.RefreshTokenRepository;
import ktb.leafresh.backend.global.config.SecurityProperties;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.security.*;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthLoginService {

  @Getter
  @Value("${kakao.client-id}")
  private String clientId;

  private final OAuthKakaoService oAuthKakaoService;
  private final RewardGrantService rewardGrantService;
  private final JwtLogoutService jwtLogoutService;
  private final JwtProvider jwtProvider;
  private final TokenProvider tokenProvider;
  private final OAuthTokenFactory tokenFactory;
  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AuthCookieProvider authCookieProvider;
  private final SecurityProperties securityProperties;

  public String getRedirectUrl(String origin) {
    if (origin == null || origin.isBlank()) {
      origin = "https://leafresh.app";
    }

    if (!securityProperties.getAllowedOrigins().contains(origin)) {
      log.warn("허용되지 않은 origin 요청: {}", origin);
      throw new CustomException(GlobalErrorCode.INVALID_ORIGIN);
    }

    String encodedRedirectUri = origin + "/member/kakao/callback";

    return "https://kauth.kakao.com/oauth/authorize"
        + "?client_id="
        + clientId
        + "&redirect_uri="
        + encodedRedirectUri
        + "&response_type=code";
  }

  @Transactional
  public OAuthTokenResponseDto loginWithKakao(String authorizationCode, String redirectUri) {
    log.info(
        "[OAuthLoginService] 카카오 로그인 시작 - code={}, redirectUri={}", authorizationCode, redirectUri);

    try {
      OAuthUserInfoDto userInfo = oAuthKakaoService.getUserInfo(authorizationCode, redirectUri);
      Optional<Member> optionalMember = memberRepository.findByEmail(userInfo.getEmail());

      if (optionalMember.isPresent()) {
        return createTokenResponseForExistingMember(optionalMember.get(), userInfo);
      }

      return createResponseForNewUser(userInfo);
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      throw new CustomException(MemberErrorCode.KAKAO_LOGIN_FAILED, e.getMessage());
    }
  }

  @Transactional
  public void logout(String accessToken, String refreshToken) {
    jwtLogoutService.logout(accessToken, refreshToken);
  }

  public OAuthTokenResponseDto reissueToken(String refreshToken) {
    String memberId = tokenProvider.getSubject(refreshToken);
    RefreshToken savedToken = validateRefreshToken(refreshToken, memberId);
    Member member = findMemberOrThrow(Long.parseLong(memberId));

    TokenDto tokenDto =
        jwtProvider.generateTokenDto(member.getId(), createAuthentication(member).getAuthorities());
    return tokenFactory.create(member, tokenDto);
  }

  public ResponseCookie createAccessTokenCookie(String accessToken, Long expiresIn) {
    return authCookieProvider.createAccessTokenCookie(accessToken);
  }

  private OAuthTokenResponseDto createTokenResponseForExistingMember(
      Member member, OAuthUserInfoDto userInfo) {
    Authentication authentication = createAuthentication(member);
    TokenDto tokenDto =
        jwtProvider.generateTokenDto(member.getId(), authentication.getAuthorities());

    saveRefreshToken(member.getId(), tokenDto.getRefreshToken());

    rewardGrantService.grantDailyLoginReward(member);

    return tokenFactory.create(member, tokenDto);
  }

  private Authentication createAuthentication(Member member) {
    UserDetails userDetails =
        new User(
            member.getId().toString(),
            "",
            List.of(new SimpleGrantedAuthority(member.getRole().name())));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  private void saveRefreshToken(Long memberId, String refreshTokenValue) {
    RefreshToken refreshToken =
        RefreshToken.builder().rtKey(String.valueOf(memberId)).rtValue(refreshTokenValue).build();
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
      throw new CustomException(GlobalErrorCode.INVALID_TOKEN);
    }

    RefreshToken saved =
        refreshTokenRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(GlobalErrorCode.REFRESH_TOKEN_NOT_FOUND));

    if (!saved.getRtValue().equals(refreshToken)) {
      throw new CustomException(GlobalErrorCode.INVALID_TOKEN);
    }

    return saved;
  }

  private Member findMemberOrThrow(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
  }
}

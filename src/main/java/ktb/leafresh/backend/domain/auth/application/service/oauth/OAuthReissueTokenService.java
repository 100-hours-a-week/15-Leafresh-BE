package ktb.leafresh.backend.domain.auth.application.service.oauth;

import ktb.leafresh.backend.domain.auth.domain.entity.RefreshToken;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthTokenResponseDto;
import ktb.leafresh.backend.domain.auth.application.factory.OAuthTokenFactory;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.RefreshTokenRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.security.TokenProvider;
import ktb.leafresh.backend.global.security.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthReissueTokenService {

  private final TokenProvider tokenProvider;
  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final OAuthTokenFactory tokenFactory;

  @Transactional(readOnly = true)
  public OAuthTokenResponseDto reissue(String refreshToken) {
    String memberId = tokenProvider.getSubject(refreshToken);

    RefreshToken savedToken = validateRefreshToken(refreshToken, memberId);
    Member member = findMemberOrThrow(Long.parseLong(memberId));

    TokenDto newToken = tokenProvider.generateTokenDto(member.getId());
    return tokenFactory.create(member, newToken);
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
      throw new CustomException(GlobalErrorCode.FORBIDDEN);
    }

    return saved;
  }

  private Member findMemberOrThrow(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 회원입니다."));
  }
}

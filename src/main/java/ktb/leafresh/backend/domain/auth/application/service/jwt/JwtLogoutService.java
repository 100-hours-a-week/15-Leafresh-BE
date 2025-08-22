package ktb.leafresh.backend.domain.auth.application.service.jwt;

import ktb.leafresh.backend.domain.auth.domain.entity.RefreshToken;
import ktb.leafresh.backend.domain.member.infrastructure.repository.RefreshTokenRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.security.JwtProvider;
import ktb.leafresh.backend.global.security.TokenBlacklistService;
import ktb.leafresh.backend.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JwtLogoutService {

  private final TokenProvider tokenProvider;
  private final JwtProvider jwtProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenBlacklistService tokenBlacklistService;

  @Transactional
  public void logout(String accessToken, String refreshToken) {
    validateRefreshToken(refreshToken);

    String memberId = tokenProvider.getAuthentication(accessToken).getName();

    deleteRefreshToken(memberId);
    blacklistAccessToken(accessToken);
  }

  private void validateRefreshToken(String refreshToken) {
    if (!tokenProvider.validateToken(refreshToken)) {
      throw new CustomException(GlobalErrorCode.INVALID_TOKEN);
    }
  }

  private void deleteRefreshToken(String memberId) {
    RefreshToken refreshToken =
        refreshTokenRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(GlobalErrorCode.REFRESH_TOKEN_NOT_FOUND));
    refreshTokenRepository.delete(refreshToken);
  }

  private void blacklistAccessToken(String accessToken) {
    long now = System.currentTimeMillis();
    long exp = jwtProvider.getExpiration(accessToken);
    long remainingTime = exp - now;
    if (remainingTime > 0) {
      tokenBlacklistService.blacklistAccessToken(accessToken, remainingTime);
    }
  }
}
